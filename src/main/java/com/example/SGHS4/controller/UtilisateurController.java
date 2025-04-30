package com.example.SGHS4.controller;

import com.example.SGHS4.dto.PersonnelDTO;
import com.example.SGHS4.dto.AuthentificationDTO;
import com.example.SGHS4.service.JwtService;
import com.example.SGHS4.service.UtilisateurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping
public class UtilisateurController {

    private final UtilisateurService utilisateurService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UtilisateurController(UtilisateurService utilisateurService,
                                 AuthenticationManager authenticationManager,
                                 JwtService jwtService) {
        this.utilisateurService = utilisateurService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/inscription")
    public ResponseEntity<?> inscription(@RequestBody PersonnelDTO dto) {
        try {
            utilisateurService.inscription(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body("Utilisateur inscrit avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur lors de l'inscription : " + e.getMessage());
        }
    }

    @PostMapping("/activation")
    public ResponseEntity<?> activation(@RequestBody Map<String, String> activation) {
        try {
            utilisateurService.activation(activation);
            return ResponseEntity.ok("Activation réussie.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur d'activation : " + e.getMessage());
        }
    }

    @PostMapping("/refresh-token")
    public @ResponseBody Map<String, String> refreshToken(@RequestBody Map<String, String> refreshTokenRequest) {
        return this.jwtService.refreshToken(refreshTokenRequest);
    }

    @PostMapping("/deconnexion")
    public void deconnexion() {
        this.jwtService.deconnexion();
    }

    @PostMapping("/connexion")
    public ResponseEntity<?> connexion(@RequestBody AuthentificationDTO authenticationDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationDTO.email(), authenticationDTO.password()
                    )
            );
            if (authentication.isAuthenticated()) {
                Map<String, String> tokenMap = jwtService.generate(authenticationDTO.email());
                return ResponseEntity.ok(tokenMap);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Identifiants incorrects.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Erreur de connexion : " + e.getMessage());
        }
    }
}
