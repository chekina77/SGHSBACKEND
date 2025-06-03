package com.example.SGHS4.securiter;

import com.example.SGHS4.entite.Jwt;
import com.example.SGHS4.exceptions.TokenInvalideException;
import com.example.SGHS4.service.JwtService;
import com.example.SGHS4.service.UtilisateurService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final UtilisateurService utilisateurService;
    private final JwtService jwtService;

    @Value("${security.public-paths}")
    private String[] publicPathsArray;

    private List<String> publicPaths;

    private static final List<String> EXTRA_PUBLIC_PATHS = List.of(
            "/api/appointments/empreinte",
            "/api/appointments/empreinte/**",
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
            System.out.println("🔍 Requête interceptée : " + path+"c'est passe");

            return;
        }

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Accès interdit : Token manquant ou mal formé");
            response.getWriter().flush();
            return;
        }

        String token = authorizationHeader.substring(7);

        try {
            authenticateWithToken(token);
            filterChain.doFilter(request, response);
        } catch (TokenInvalideException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token invalide : " + e.getMessage());
            response.getWriter().flush();
        }
    }

    private void authenticateWithToken(String token) {
        String username = jwtService.extractUsername(token);
        Jwt tokenEnBase = jwtService.tokenByValue(token);

        if (username == null || tokenEnBase == null || jwtService.isTokenExpired(token)) {
            throw new TokenInvalideException("Token invalide ou expiré");
        }

        if (!username.equals(tokenEnBase.getUtilisateur().getEmail())) {
            throw new TokenInvalideException("Token ne correspond pas à l'utilisateur");
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
        return publicPaths.stream().anyMatch(pattern -> {
            if (pattern.endsWith("/**")) {
                return path.startsWith(pattern.substring(0, pattern.length() - 3));
            } else {
                return path.equals(pattern);
            }
        }) || EXTRA_PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
}
