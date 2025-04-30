package com.example.SGHS4.controller;

import com.example.SGHS4.dto.PatientDTO;
import com.example.SGHS4.exceptions.ValidationException;
import com.example.SGHS4.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patient")
@Tag(name = "Patient", description = "API de gestion des patients")
@PreAuthorize("hasAnyRole('INFIRMIER', 'MEDECIN', 'ADMINISTRATEUR')")
public class PatientController {

    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    /**
     * Crée un nouveau patient
     */
    @PostMapping
    @Operation(
            summary = "Créer un nouveau patient",
            description = "Permet à un infirmier ou un médecin d'enregistrer un nouveau patient dans le système",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Patient créé avec succès"),
                    @ApiResponse(responseCode = "400", description = "Données de patient invalides"),
                    @ApiResponse(responseCode = "401", description = "Non authentifié"),
                    @ApiResponse(responseCode = "403", description = "Non autorisé")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> createPatient(@Valid @RequestBody PatientDTO patientDTO) {
        logger.info("Demande de création d'un nouveau patient");
        try {
            PatientDTO createdPatient = patientService.createPatient(patientDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Patient créé avec succès");
            response.put("patient", createdPatient);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ValidationException e) {
            logger.warn("Erreur de validation lors de la création d'un patient: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la création d'un patient", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Une erreur s'est produite lors de la création du patient");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Récupère un patient par son ID
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Récupérer un patient par ID",
            description = "Permet de récupérer les informations d'un patient spécifique via son identifiant",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Patient trouvé"),
                    @ApiResponse(responseCode = "404", description = "Patient non trouvé"),
                    @ApiResponse(responseCode = "401", description = "Non authentifié"),
                    @ApiResponse(responseCode = "403", description = "Non autorisé")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> getPatientById(
            @Parameter(description = "ID du patient", required = true)
            @PathVariable Long id
    ) {
        logger.info("Demande de récupération du patient avec ID: {}", id);
        try {
            PatientDTO patient = patientService.getPatientById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("patient", patient);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warn("Patient avec ID {} non trouvé", id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Patient non trouvé");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération du patient avec ID: {}", id, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Une erreur s'est produite");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Récupère tous les patients
     */
    @GetMapping
    @Operation(
            summary = "Récupérer tous les patients",
            description = "Permet de récupérer la liste de tous les patients enregistrés",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Liste des patients récupérée avec succès"),
                    @ApiResponse(responseCode = "401", description = "Non authentifié"),
                    @ApiResponse(responseCode = "403", description = "Non autorisé")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> getAllPatients() {
        logger.info("Demande de récupération de tous les patients");
        try {
            List<PatientDTO> patients = patientService.getAllPatients();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", patients.size());
            response.put("patients", patients);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de tous les patients", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Une erreur s'est produite lors de la récupération des patients");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Met à jour un patient existant
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Mettre à jour un patient",
            description = "Permet de mettre à jour les informations d'un patient existant",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Patient mis à jour avec succès"),
                    @ApiResponse(responseCode = "400", description = "Données de patient invalides"),
                    @ApiResponse(responseCode = "404", description = "Patient non trouvé"),
                    @ApiResponse(responseCode = "401", description = "Non authentifié"),
                    @ApiResponse(responseCode = "403", description = "Non autorisé")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> updatePatient(
            @Parameter(description = "ID du patient", required = true)
            @PathVariable Long id,
            @Valid @RequestBody PatientDTO patientDTO
    ) {
        logger.info("Demande de mise à jour du patient avec ID: {}", id);
        try {
            PatientDTO updatedPatient = patientService.updatePatient(id, patientDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Patient mis à jour avec succès");
            response.put("patient", updatedPatient);

            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            logger.warn("Erreur de validation lors de la mise à jour du patient: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (RuntimeException e) {
            logger.warn("Patient avec ID {} non trouvé pour la mise à jour", id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Patient non trouvé");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour du patient avec ID: {}", id, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Une erreur s'est produite lors de la mise à jour du patient");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Supprime un patient
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Supprimer un patient",
            description = "Permet de supprimer un patient du système",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Patient supprimé avec succès"),
                    @ApiResponse(responseCode = "404", description = "Patient non trouvé"),
                    @ApiResponse(responseCode = "401", description = "Non authentifié"),
                    @ApiResponse(responseCode = "403", description = "Non autorisé")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('MEDECIN')")
    public ResponseEntity<Map<String, Object>> deletePatient(
            @Parameter(description = "ID du patient", required = true)
            @PathVariable Long id
    ) {
        logger.info("Demande de suppression du patient avec ID: {}", id);
        try {
            patientService.deletePatient(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Patient supprimé avec succès");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warn("Patient avec ID {} non trouvé pour la suppression", id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Patient non trouvé");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression du patient avec ID: {}", id, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Une erreur s'est produite lors de la suppression du patient");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Recherche des patients par nom ou prénom
     */
    @GetMapping("/search")
    @Operation(
            summary = "Rechercher des patients",
            description = "Permet de rechercher des patients par nom ou prénom",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Recherche effectuée avec succès"),
                    @ApiResponse(responseCode = "401", description = "Non authentifié"),
                    @ApiResponse(responseCode = "403", description = "Non autorisé")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> searchPatients(
            @Parameter(description = "Terme de recherche (nom ou prénom)", required = true)
            @RequestParam String query
    ) {
        logger.info("Recherche de patients avec le terme: {}", query);
        try {
            List<PatientDTO> patients = patientService.searchPatients(query);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", patients.size());
            response.put("patients", patients);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de patients", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Une erreur s'est produite lors de la recherche");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}