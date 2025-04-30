package com.example.SGHS4.securiter;

import com.example.SGHS4.entite.Jwt;
import com.example.SGHS4.service.JwtService;
import com.example.SGHS4.service.UtilisateurService;
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

@Component // Permet à Spring de gérer cette classe comme un bean
public class JwtFilter extends OncePerRequestFilter {

    private final UtilisateurService utilisateurService;
    private final JwtService jwtService;

    // Constructeur pour l'injection des dépendances
    public JwtFilter(UtilisateurService utilisateurService, JwtService jwtService) {
        this.utilisateurService = utilisateurService;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Vérifier si la requête est pour un des chemins d'authentification (connexion, inscription, etc.)
        String path = request.getServletPath();
        if (path.equals("/connexion") ||
                path.equals("/inscription") ||
                path.equals("/activation") ||
                path.equals("/refresh-token") ||
                path.startsWith("/admin/pending-personnel") ||
                path.equals("/admin/complete-registration") ||
                path.equals("/enregistrements/enregistrer") ||
                path.equals("/enregistrements/allenregistrer") ||


                path.equals("/admin/connexion")) {

            filterChain.doFilter(request, response);
            return; // Autoriser ces chemins sans vérifier le JWT
        }


        // Récupérer le header "Authorization" de la requête
        String authorization = request.getHeader("Authorization");
        String token = null;
        String username = null;
        Jwt tokenDansLaBDD = null;
        boolean isTokenExpired = true;

        // Si un token est présent dans le header "Authorization", extraire le token et son nom d'utilisateur
        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7); // Extraire le token après "Bearer "
            username = jwtService.extractUsername(token); // Extraire le nom d'utilisateur du token
            tokenDansLaBDD = jwtService.tokenByValue(token); // Vérifier si le token est valide dans la base de données
            isTokenExpired = jwtService.isTokenExpired(token); // Vérifier si le token est expiré
        }

        // Si toutes les conditions sont remplies (token valide, non expiré, utilisateur existant)
        if (token != null &&
                username != null &&
                tokenDansLaBDD != null &&
                !isTokenExpired &&
                tokenDansLaBDD.getUtilisateur().getEmail().equals(username) &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            // Charger les détails de l'utilisateur et authentifier la requête
            UserDetails userDetails = utilisateurService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken); // Mettre l'authentification dans le contexte
        }

        // Passer la requête au filtre suivant
        filterChain.doFilter(request, response);
    }
}
