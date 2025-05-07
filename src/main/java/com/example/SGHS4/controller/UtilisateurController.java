package com.example.SGHS4.controller;

import com.example.SGHS4.dto.*;
import com.example.SGHS4.entite.Utilisateur;
import com.example.SGHS4.exceptions.ValidationException;
import com.example.SGHS4.service.JwtService;
import com.example.SGHS4.service.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping
@Tag(name = "Authentification", description = "API d'authentification et de gestion des utilisateurs")
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
    @Operation(
            summary = "Inscription d'un nouvel utilisateur",
            description = "Permet d'inscrire un nouvel utilisateur dans le système. Un email d'activation sera envoyé.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Utilisateur inscrit avec succès"),
                    @ApiResponse(responseCode = "400", description = "Données d'inscription invalides"),
                    @ApiResponse(responseCode = "409", description = "Email, téléphone ou CNI déjà utilisé")
            }
    )
    public ResponseEntity<?> inscription(@Valid @RequestBody PendingPersonnelDTO dto) {
        utilisateurService.inscription(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Utilisateur inscrit avec succès. Veuillez vérifier votre email pour l'activation."));
    }

    @PostMapping("/activation")
    @Operation(
            summary = "Activation d'un compte utilisateur",
            description = "Active un compte utilisateur à l'aide du code de validation reçu par email",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Activation réussie"),
                    @ApiResponse(responseCode = "400", description = "Code d'activation invalide ou expiré")
            }
    )
    public ResponseEntity<?> activation(@RequestBody Map<String, String> activation) {
        utilisateurService.activation(activation);
        return ResponseEntity.ok(Map.of("message", "Activation réussie. Vous pouvez maintenant vous connecter."));
    }

    @PostMapping("/connexion")
    @Operation(
            summary = "Connexion utilisateur",
            description = "Authentifie un utilisateur et fournit des tokens JWT",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Connexion réussie"),
                    @ApiResponse(responseCode = "401", description = "Identifiants incorrects")
            }
    )
    public ResponseEntity<?> connexion(@Valid @RequestBody AuthentificationDTO authenticationDTO) {
        // Authentifier l'utilisateur
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationDTO.email(), authenticationDTO.password()
                )
        );

        if (authentication.isAuthenticated()) {
            // Récupérer les informations de l'utilisateur
            Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();

            // Générer les tokens
            Map<String, String> tokenMap = jwtService.generate(authenticationDTO.email());

            // Transformer la liste d'authorities en liste de strings de rôles
            List<String> roles = utilisateur.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            // Créer la réponse
            JwtResponseDTO response = new JwtResponseDTO(
                    tokenMap.get("bearer"),
                    tokenMap.get("refresh"),
                    utilisateur.getEmail(),
                    utilisateur.getNom(),
                    roles
            );

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Identifiants incorrects"));
        }
    }

    @PostMapping("/refresh-token")
    @Operation(
            summary = "Rafraîchissement du token",
            description = "Génère un nouveau token JWT à partir d'un refresh token valide",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Nouveaux tokens générés"),
                    @ApiResponse(responseCode = "401", description = "Refresh token invalide ou expiré")
            }
    )
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> refreshTokenRequest) {
        Map<String, String> tokens = this.jwtService.refreshToken(refreshTokenRequest);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/deconnexion")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Déconnexion",
            description = "Invalide le token JWT actuel",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Déconnexion réussie"),
                    @ApiResponse(responseCode = "401", description = "Non authentifié")
            }
    )
    public ResponseEntity<?> deconnexion() {
        this.jwtService.deconnexion();
        return ResponseEntity.ok(Map.of("message", "Déconnexion réussie"));
    }

    /**
     * Modifie le mot de passe de l'utilisateur connecté
     */
    @PostMapping("/envoyer-code-reinitialisation")
    public ResponseEntity<?> envoyerCode(@RequestBody DemandeReinitialisationDTO request) {
        String email = request.getEmail(); // Utilise le getter de l'objet
        utilisateurService.envoyerCodeReinitialisation(email);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Code envoyé avec succès à " + email);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/modifier-mot-de-passe")
    public ResponseEntity<?> modifierMotDePasse(@RequestBody @Valid ModificationMdpDTO dto) {
        try {
            utilisateurService.modifierMotDePasse(dto);
            return ResponseEntity.ok(Map.of("success", true, "message", "Mot de passe modifié avec succès"));
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Erreur interne"));
        }
    }
    @PostMapping("/envoyer-nouveau-code")
    public ResponseEntity<String> envoyerNouveauCode(@RequestBody VerificationCodeDTO emailDTO) {
        try {
            utilisateurService.verifierEtEnvoyerNouveauCode(VerificationCodeDTO.getEmail());
            return ResponseEntity.ok("Nouveau code de réinitialisation envoyé.");
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur : " + e.getMessage());
        }
    }

}