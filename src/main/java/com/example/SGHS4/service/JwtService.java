package com.example.SGHS4.service;

import com.example.SGHS4.dto.RefreshTokenRequest;
import com.example.SGHS4.entite.Jwt;
import com.example.SGHS4.entite.RefreshToken;
import com.example.SGHS4.entite.Utilisateur;
import com.example.SGHS4.exceptions.TokenExpireException;
import com.example.SGHS4.exceptions.TokenInvalideException;
import com.example.SGHS4.repository.JwtRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    public static final String BEARER = "bearer";
    public static final String REFRESH = "refresh";
    private static final String TOKEN_INVALIDE = "Token invalide";

    @Value("${jwt.encryption.key}")
    private String encryptionKey;

    @Value("${jwt.expiration.bearer:3600000}") // 1 heure par défaut
    private long bearerExpirationMs;

    @Value("${jwt.expiration.refresh:86400000}") //  24 heures par défaut
    private long refreshExpirationMs;

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
        ).orElseThrow(() -> new TokenInvalideException("Token invalide ou inconnu"));
    }

    // Générez les tokens JWT et Refresh Token
    public Map<String, String> generate(String username) {
        Utilisateur utilisateur = (Utilisateur) this.utilisateurService.loadUserByUsername(username);

        // Vérifier si l'utilisateur est actif
        if (!utilisateur.isEnabled()) {
            throw new RuntimeException("Compte utilisateur inactif");
        }

        this.disableTokens(utilisateur);  // Désactive les anciens tokens

        final Map<String, String> jwtMap = new HashMap<>(this.generateJwt(utilisateur));

        // Créer un Refresh Token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setValeur(UUID.randomUUID().toString());
        refreshToken.setExpire(false);
        refreshToken.setCreation(Instant.now());
        refreshToken.setExpiration(Instant.now().plusMillis(refreshExpirationMs));

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
        try {
            return this.getClaim(token, Claims::getSubject);
        } catch (ExpiredJwtException e) {
            throw new TokenExpireException("Le token JWT a expiré, veuillez vous reconnecter.");
        } catch (JwtException e) {
            throw new TokenInvalideException("Le token JWT est invalide.");
        }
    }

    // Vérifier si le token est expiré
    public boolean isTokenExpired(String token) {
        try {
            Date expirationDate = getExpirationDateFromToken(token);
            return expirationDate.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException e) {
            throw new TokenInvalideException("Le token JWT est invalide.");
        }
    }

    private Date getExpirationDateFromToken(String token) {
        return this.getClaim(token, Claims::getExpiration);
    }

    private <T> T getClaim(String token, Function<Claims, T> function) {
        Claims claims = getAllClaims(token);
        return function.apply(claims);
    }

    private Claims getAllClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(this.getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            logger.error("Erreur lors de l'analyse du JWT", e);
            throw new TokenInvalideException("Le token JWT est invalide.");
        }
    }

    // Générer le JWT
    Map<String, String> generateJwt(Utilisateur utilisateur) {
        final long currentTime = System.currentTimeMillis();
        final long expirationTime = currentTime + bearerExpirationMs;

        // Récupérer les rôles de l'utilisateur
        List<String> roles = utilisateur.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toList());

        // Ajouter les roles dans les claims
        final Map<String, Object> claims = new HashMap<>();
        claims.put("nom", utilisateur.getNom());
        claims.put("roles", roles);
        claims.put(Claims.EXPIRATION, new Date(expirationTime));
        claims.put(Claims.SUBJECT, utilisateur.getEmail());
        claims.put(Claims.ISSUED_AT, new Date(currentTime));

        // Générer le token JWT
        final String bearer = Jwts.builder()
                .setClaims(claims)
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();

        return Map.of(BEARER, bearer);
    }

    private Key getKey() {
        final byte[] decoder = Decoders.BASE64.decode(encryptionKey);
        return Keys.hmacShaKeyFor(decoder);
    }

    // Déconnexion et désactivation du token
    public void deconnexion() {
        try {
            Utilisateur utilisateur = (Utilisateur) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Jwt jwt = this.jwtRepository.findUtilisateurValidToken(
                    utilisateur.getEmail(),
                    false,
                    false
            ).orElseThrow(() -> new TokenInvalideException(TOKEN_INVALIDE));

            jwt.setExpire(true);
            jwt.setDesactive(true);
            this.jwtRepository.save(jwt);

            // Effacer le contexte de sécurité
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            logger.error("Erreur lors de la déconnexion", e);
            throw new RuntimeException("Erreur lors de la déconnexion.");
        }
    }

    // Suppression des tokens expirés ou inutiles
    @Scheduled(cron = "${jwt.cleanup.cron:0 0 0 * * ?}") // Par défaut: tous les jours à minuit
    public void removeUselessJwt() {
        logger.info("Suppression des tokens expirés à " + Instant.now());
        try {
            long count = this.jwtRepository.deleteAllByExpireAndDesactive(true, true);
            logger.info("{} tokens supprimés", count);
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression des tokens expirés", e);
        }
    }

    // Rafraîchir le token
    public Map<String, String> refreshToken(RefreshTokenRequest refreshTokenRequest) {
        if (refreshTokenRequest.getRefreshToken() == null || refreshTokenRequest.getRefreshToken().isEmpty()) {
            throw new TokenInvalideException("Refresh token manquant");
        }

        final Jwt jwt = this.jwtRepository.findByRefreshToken(refreshTokenRequest.getRefreshToken())
                .orElseThrow(() -> new TokenInvalideException("Refresh token invalide"));

        // Vérifier que le refresh token est valide
        if (jwt.getRefreshToken().isExpire() || jwt.getRefreshToken().getExpiration().isBefore(Instant.now())) {
            throw new TokenInvalideException("Refresh token expiré, veuillez vous reconnecter");
        }

        // Désactiver les anciens tokens
        this.disableTokens(jwt.getUtilisateur());

        // Générer un nouveau jeu de tokens
        return this.generate(jwt.getUtilisateur().getEmail());
    }
}
