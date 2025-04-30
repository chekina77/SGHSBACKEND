package com.example.SGHS4.service;

import com.example.SGHS4.entite.Jwt;
import com.example.SGHS4.entite.RefreshToken;
import com.example.SGHS4.entite.Utilisateur;
import com.example.SGHS4.repository.JwtRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Key;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Transactional
@Service
public class JwtService {

    public static final String BEARER = "bearer";
    public static final String REFRESH = "refresh";
    private static final String TOKEN_INVALIDE = "Token invalide";

    private static final String ENCRYPTION_KEY = "YWJmYjNmNjhlNmM2MWRkZmYyMGI0YjA2MTZmMGQ5MTcxZjc1MDNiNTFlN2FkODE3MjUwYzJmMGQzOThjZjk5NQ==";

    private final UtilisateurService utilisateurService;
    private final JwtRepository jwtRepository;

    public JwtService(UtilisateurService utilisateurService, JwtRepository jwtRepository) {
        this.utilisateurService = utilisateurService;
        this.jwtRepository = jwtRepository;
    }

    public Jwt tokenByValue(String value) {
        return this.jwtRepository.findByValueAndDesactiveAndExpire(
                value,
                false,
                false
        ).orElseThrow(() -> new RuntimeException("Token invalide ou inconnu"));
    }

    // Générez les tokens JWT et Refresh Token
    public Map<String, String> generate(String username) {
        Utilisateur utilisateur = (Utilisateur) this.utilisateurService.loadUserByUsername(username);
        this.disableTokens(utilisateur);  // Désactive les anciens tokens

        final Map<String, String> jwtMap = new HashMap<>(this.generateJwt(utilisateur));

        // Créer un Refresh Token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setValeur(UUID.randomUUID().toString());
        refreshToken.setExpire(false);
        refreshToken.setCreation(Instant.now());
        refreshToken.setExpiration(Instant.now().plusMillis(30 * 60 * 1000));  // Expire après 30 minutes

        // Créer un Jwt avec un lien vers le Refresh Token
        Jwt jwt = new Jwt();
        jwt.setValue(jwtMap.get(BEARER));
        jwt.setDesactive(false);
        jwt.setExpire(false);
        jwt.setUtilisateur(utilisateur);
        jwt.setRefreshToken(refreshToken);  // Lier le Refresh Token au Jwt

        // Sauvegarder le JWT et le Refresh Token dans la base de données
        this.jwtRepository.save(jwt);
        jwtMap.put(REFRESH, refreshToken.getValeur());  // Ajouter le Refresh Token au retour

        return jwtMap;
    }

    // Désactiver tous les tokens existants pour un utilisateur
    private void disableTokens(Utilisateur utilisateur) {
        List<Jwt> jwtList = this.jwtRepository.findUtilisateur(utilisateur.getEmail())
                .collect(Collectors.toList());

        jwtList.forEach(jwt -> {
            jwt.setDesactive(true);
            jwt.setExpire(true);  // Marquer les tokens comme expirés
        });

        this.jwtRepository.saveAll(jwtList);
    }

    // Extraire le nom d'utilisateur à partir du token
    public String extractUsername(String token) {
        return this.getClaim(token, Claims::getSubject);
    }

    // Vérifier si le token est expiré
    public boolean isTokenExpired(String token) {
        Date expirationDate = getExpirationDateFromToken(token);
        return expirationDate.before(new Date());
    }

    private Date getExpirationDateFromToken(String token) {
        return this.getClaim(token, Claims::getExpiration);
    }

    private <T> T getClaim(String token, Function<Claims, T> function) {
        Claims claims = getAllClaims(token);
        return function.apply(claims);
    }

    private Claims getAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(this.getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Générer le JWT
    // Générer le JWT
    Map<String, String> generateJwt(Utilisateur utilisateur) {
        final long currentTime = System.currentTimeMillis();
        final long expirationTime = currentTime + 60 * 60 * 1000;  // 1 heure

        // Récupérer les rôles de l'utilisateur
        List<String> roles = utilisateur.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority()) // Récupère le rôle
                .collect(Collectors.toList());

        // Ajouter les roles dans les claims
        final Map<String, Object> claims = new HashMap<>();
        claims.put("nom", utilisateur.getNom());
        claims.put("roles", roles);  // Ajouter le rôle dans les claims
        claims.put(Claims.EXPIRATION, new Date(expirationTime));
        claims.put(Claims.SUBJECT, utilisateur.getEmail());

        final String bearer = Jwts.builder()
                .setIssuedAt(new Date(currentTime))
                .setExpiration(new Date(expirationTime))
                .setSubject(utilisateur.getEmail())
                .setClaims(claims)
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();

        return Map.of(BEARER, bearer);
    }


    private Key getKey() {
        final byte[] decoder = Decoders.BASE64.decode(ENCRYPTION_KEY);
        return Keys.hmacShaKeyFor(decoder);
    }

    // Déconnexion et désactivation du token
    public void deconnexion() {
        Utilisateur utilisateur = (Utilisateur) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Jwt jwt = this.jwtRepository.findUtilisateurValidToken(
                utilisateur.getEmail(),
                false,
                false
        ).orElseThrow(() -> new RuntimeException(TOKEN_INVALIDE));
        jwt.setExpire(true);
        jwt.setDesactive(true);
        this.jwtRepository.save(jwt);
    }

    // Suppression des tokens expirés ou inutiles
    @Scheduled(cron = "@daily")
    public void removeUselessJwt() {
        System.out.println("Suppression des token " + Instant.now());
        this.jwtRepository.deleteAllByExpireAndDesactive(true, true);
    }

    // Rafraîchir le token
    public Map<String, String> refreshToken(Map<String, String> refreshTokenRequest) {
        final Jwt jwt = this.jwtRepository.findByRefreshToken(refreshTokenRequest.get(REFRESH))
                .orElseThrow(() -> new RuntimeException(TOKEN_INVALIDE));

        if(jwt.getRefreshToken().isExpire() || jwt.getRefreshToken().getExpiration().isBefore(Instant.now())) {
            throw new RuntimeException(TOKEN_INVALIDE);
        }
        this.disableTokens(jwt.getUtilisateur());
        return this.generate(jwt.getUtilisateur().getEmail());
    }
}
