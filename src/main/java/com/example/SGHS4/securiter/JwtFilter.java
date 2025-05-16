package com.example.SGHS4.securiter;

import com.example.SGHS4.entite.Jwt;
import com.example.SGHS4.exceptions.TokenInvalideException;
import com.example.SGHS4.service.JwtService;
import com.example.SGHS4.service.UtilisateurService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final UtilisateurService utilisateurService;
    private final JwtService jwtService;

    @Value("${security.public-paths}")
    private String[] publicPathsArray;

    private List<String> publicPaths;

    // ✅ Ajout explicite de chemins publics supplémentaires
    private static final List<String> EXTRA_PUBLIC_PATHS = List.of(
            "/api/biometric/process-fingerprint",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-ui.html"
    );

    public JwtFilter(UtilisateurService utilisateurService, JwtService jwtService) {
        this.utilisateurService = utilisateurService;
        this.jwtService = jwtService;
    }

    @PostConstruct
    public void init() {
        this.publicPaths = Arrays.asList(publicPathsArray);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        System.out.println("🔍 Requête interceptée : " + path);

        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Accès interdit : Token manquant ou mal formé");
            response.getWriter().flush();
            return;
        }

        try {
            String token = authorization.substring(7);
            processToken(token);
            filterChain.doFilter(request, response);
        } catch (TokenInvalideException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(e.getMessage());
            response.getWriter().flush();
        }
    }

    private void processToken(String token) {
        String username = jwtService.extractUsername(token);
        Jwt tokenDansLaBDD = jwtService.tokenByValue(token);
        boolean isTokenExpired = jwtService.isTokenExpired(token);

        if (token == null ||
                username == null ||
                tokenDansLaBDD == null ||
                isTokenExpired ||
                !tokenDansLaBDD.getUtilisateur().getEmail().equals(username)) {
            throw new TokenInvalideException("Token invalide ou expiré");
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = utilisateurService.loadUserByUsername(username);

            if (!userDetails.isEnabled()) {
                throw new TokenInvalideException("Compte utilisateur inactif");
            }

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }

    private boolean isPublicPath(String path) {
        // 🔍 Chemins définis dans le fichier properties
        boolean fromProperties = publicPaths.stream().anyMatch(pattern -> {
            if (pattern.endsWith("/**")) {
                String basePath = pattern.substring(0, pattern.length() - 3);
                return path.startsWith(basePath);
            }
            return path.equals(pattern);
        });

        // 🔍 Chemins ajoutés en dur
        boolean fromExtra = EXTRA_PUBLIC_PATHS.stream().anyMatch(path::startsWith);

        return fromProperties || fromExtra;
    }
}