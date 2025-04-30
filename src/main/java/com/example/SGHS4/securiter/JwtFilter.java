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

    public JwtFilter(UtilisateurService utilisateurService, JwtService jwtService) {
        this.utilisateurService = utilisateurService;
        this.jwtService = jwtService;
    }

    @Override
    protected void initFilterBean() {
        this.publicPaths = Arrays.asList(publicPathsArray);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // Vérifier si le chemin est public
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Récupérer le header "Authorization" de la requête
        String authorization = request.getHeader("Authorization");

        // Si aucun token n'est présent, on continue la chaîne de filtres
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extraire et valider le token
            String token = authorization.substring(7);
            processToken(token);

            // Passer la requête au filtre suivant
            filterChain.doFilter(request, response);
        } catch (TokenInvalideException e) {
            // En cas d'erreur de token, on renvoie une erreur 401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(e.getMessage());
            response.getWriter().flush();
        }
    }

    private void processToken(String token) {
        // Extraire les informations du token
        String username = jwtService.extractUsername(token);
        Jwt tokenDansLaBDD = jwtService.tokenByValue(token);
        boolean isTokenExpired = jwtService.isTokenExpired(token);

        // Valider le token
        if (token == null ||
                username == null ||
                tokenDansLaBDD == null ||
                isTokenExpired ||
                !tokenDansLaBDD.getUtilisateur().getEmail().equals(username)) {

            throw new TokenInvalideException("Token invalide ou expiré");
        }

        // Si l'utilisateur n'est pas encore authentifié dans ce contexte
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            // Charger les détails de l'utilisateur et authentifier la requête
            UserDetails userDetails = utilisateurService.loadUserByUsername(username);

            // Vérifier si l'utilisateur est actif
            if (!userDetails.isEnabled()) {
                throw new TokenInvalideException("Compte utilisateur inactif");
            }

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }

    private boolean isPublicPath(String path) {
        return publicPaths.stream()
                .anyMatch(pattern -> {
                    if (pattern.endsWith("/**")) {
                        String basePath = pattern.substring(0, pattern.length() - 3);
                        return path.startsWith(basePath);
                    }
                    return path.equals(pattern);
                });
    }
}