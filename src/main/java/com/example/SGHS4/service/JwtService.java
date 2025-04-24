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

    public Map<String, String> generate(String username) {
        Utilisateur utilisateur = (Utilisateur) this.utilisateurService.loadUserByUsername(username);
        this.disableTokens(utilisateur);
        final Map<String, String> jwtMap = new HashMap<>(this.generateJwt(utilisateur));

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setValeur(UUID.randomUUID().toString());
        refreshToken.setExpire(false);
        refreshToken.setCreation(Instant.now());
        refreshToken.setExpiration(Instant.now().plusMillis(30 * 60 * 1000));

        Jwt jwt = new Jwt();
        jwt.setValue(jwtMap.get(BEARER));
        jwt.setDesactive(false);
        jwt.setExpire(false);
        jwt.setUtilisateur(utilisateur);
        jwt.setRefreshToken(refreshToken);

        this.jwtRepository.save(jwt);
        jwtMap.put(REFRESH, refreshToken.getValeur());
        return jwtMap;
    }

    private void disableTokens(Utilisateur utilisateur) {
        List<Jwt> jwtList = this.jwtRepository.findUtilisateur(utilisateur.getEmail())
                .collect(Collectors.toList());

        jwtList.forEach(jwt -> {
            jwt.setDesactive(true);
            jwt.setExpire(true);
        });

        this.jwtRepository.saveAll(jwtList);
    }

    public String extractUsername(String token) {
        return this.getClaim(token, Claims::getSubject);
    }

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

    private Map<String, String> generateJwt(Utilisateur utilisateur) {
        final long currentTime = System.currentTimeMillis();
        final long expirationTime = currentTime + 60 * 60 * 1000;

        final Map<String, Object> claims = Map.of(
                "nom", utilisateur.getNom(),
                Claims.EXPIRATION, new Date(expirationTime),
                Claims.SUBJECT, utilisateur.getEmail()
        );

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

    @Scheduled(cron = "@daily")
    public void removeUselessJwt() {
        System.out.println("Suppression des token " + Instant.now());
        this.jwtRepository.deleteAllByExpireAndDesactive(true, true);
    }

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
