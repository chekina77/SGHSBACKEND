package com.example.SGHS4.controller;

import com.example.SGHS4.dto.*;
import com.example.SGHS4.entite.PendingPersonnel;
import com.example.SGHS4.dto.RegistrationCompletionDTO;

import com.example.SGHS4.entite.Utilisateur;
import com.example.SGHS4.exceptions.UtilisateurExisteDejaException;
import com.example.SGHS4.exceptions.ValidationException;
import com.example.SGHS4.service.AdminService;
import com.example.SGHS4.repository.PendingPersonnelRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@Tag(name = "Administration", description = "API d'administration pour la gestion des utilisateurs")
@Validated
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final AdminService adminService;
    private final PendingPersonnelRepository pendingPersonnelRepository;

    public AdminController(AdminService adminService, PendingPersonnelRepository pendingPersonnelRepository) {
        this.adminService = adminService;
        this.pendingPersonnelRepository = pendingPersonnelRepository;
    }

    /**
     * Enregistre un nouveau membre du personnel
     */
    @PostMapping("/complete-registration")
    public ResponseEntity<Map<String, Object>> completeRegistration(@Valid @RequestBody RegistrationCompletionDTO payload) {
        logger.info("Tentative de finalisation d'inscription pour CNI: {}", payload.getCni());

        try {
            // Appel du service pour la finalisation d'inscription
            String result = adminService.verifyAndCompleteRegistration(payload);

            // Création de la réponse de succès
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result);
            return ResponseEntity.ok(response);

        } catch (ValidationException e) {
            // Gestion des erreurs de validation
            logger.warn("Erreur de validation: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (UtilisateurExisteDejaException e) {
            // Gestion des conflits de données (email ou téléphone déjà utilisé)
            logger.warn("Conflit: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (Exception e) {
            // Gestion des erreurs internes
            logger.error("Erreur interne lors de la finalisation de l'inscription", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Une erreur interne est survenue.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<RegistrationResponseDTO> registerPersonnel(@Valid @RequestBody PendingPersonnelDTO dto) {
        logger.info("Demande d'enregistrement d'un nouveau personnel: {}", dto.getEmail());

        try {
            String message = adminService.registerPersonnel(dto);

            // Créer une réponse structurée
            RegistrationResponseDTO response = new RegistrationResponseDTO();
            response.setSuccess(true);
            response.setMessage(message);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ValidationException e) {
            logger.warn("Échec d'enregistrement du personnel: {}", e.getMessage());

            RegistrationResponseDTO response = new RegistrationResponseDTO();
            response.setSuccess(false);
            response.setMessage(e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Erreur lors de l'enregistrement du personnel", e);

            RegistrationResponseDTO response = new RegistrationResponseDTO();
            response.setSuccess(false);
            response.setMessage("Une erreur s'est produite lors de l'enregistrement. Veuillez réessayer.");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Connecte un administrateur
     */
    @PostMapping("/connexion")
    @Operation(
            summary = "Connexion administrateur",
            description = "Authentifie un administrateur et fournit des tokens JWT",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Connexion réussie",
                            content = @Content(schema = @Schema(implementation = JwtResponseDTO.class))),
                    @ApiResponse(responseCode = "401", description = "Identifiants incorrects")
            }
    )
    public ResponseEntity<?> connexion(@Valid @RequestBody AuthentificationDTO authenticationDTO) {
        logger.info("Tentative de connexion admin pour: {}", authenticationDTO.email());

        try {
            Map<String, String> tokens = adminService.connexion(authenticationDTO);

            // Si nécessaire, créer un DTO structuré pour la réponse JWT
            JwtResponseDTO response = new JwtResponseDTO(
                    tokens.get("bearer"),
                    tokens.get("refresh"),
                    authenticationDTO.email(),
                    null, // nom (pourrait être récupéré du service)
                    Collections.singletonList("ROLE_ADMIN")
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.warn("Échec de connexion admin: {}", e.getMessage());

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Récupère les informations d'un personnel en attente via sa CNI
     */

    @GetMapping("/pending-personnel/{cni}")
    @Operation(
            summary = "Récupération d'un personnel en attente",
            description = "Récupère les informations d'un personnel en attente d'activation via son numéro CNI",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Personnel trouvé"),
                    @ApiResponse(responseCode = "404", description = "Personnel non trouvé")
            }
    )
    public ResponseEntity<PendingPersonnel> getPendingPersonnelByCni(
            @Parameter(description = "Numéro de CNI du personnel en attente", required = true)
            @PathVariable @NotBlank(message = "Le numéro CNI est obligatoire") String cni
    ) {
        logger.info("Recherche de personnel en attente avec CNI: {}", cni);

        return pendingPersonnelRepository.findByCni(cni)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.warn("Aucun personnel en attente trouvé avec CNI: {}", cni);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                });
    }


    /**
     * Finalise l'inscription d'un personnel
     */

    /**
     * Liste tous les membres du personnel (actifs et inactifs)
     */
    @GetMapping("/personnel")
    @Operation(
            summary = "Liste du personnel",
            description = "Récupère la liste de tout le personnel enregistré",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès"),
                    @ApiResponse(responseCode = "401", description = "Non autorisé"),
                    @ApiResponse(responseCode = "403", description = "Accès refusé")
            }
    )
    public ResponseEntity<?> getAllPersonnel() {
        logger.info("Demande de liste du personnel");

        try {
            return ResponseEntity.ok(adminService.getAllPersonnel());
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de la liste du personnel", e);

            Map<String, String> response = new HashMap<>();
            response.put("error", "Erreur lors de la récupération des données: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
