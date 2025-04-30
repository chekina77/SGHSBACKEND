package com.example.SGHS4.controller;

import com.example.SGHS4.dto.AppointmentResponseDTO;
import com.example.SGHS4.dto.CreateAppointmentDTO;
import com.example.SGHS4.exceptions.ResourceNotFoundException;
import com.example.SGHS4.exceptions.ValidationException;
import com.example.SGHS4.service.AppointementDoctorService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@Tag(name = "Rendez-vous médicaux", description = "API de gestion des rendez-vous avec les médecins")
public class AppointementDoctorController {

    private static final Logger logger = LoggerFactory.getLogger(AppointementDoctorController.class);

    private final AppointementDoctorService appointmentService;

    @Autowired
    public AppointementDoctorController(AppointementDoctorService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * Crée un nouveau rendez-vous
     */
    @PostMapping
    @Operation(
            summary = "Créer un nouveau rendez-vous",
            description = "Permet de programmer un rendez-vous avec un médecin",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Rendez-vous créé avec succès",
                            content = @Content(schema = @Schema(implementation = AppointmentResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Données de rendez-vous invalides"),
                    @ApiResponse(responseCode = "404", description = "Médecin non trouvé"),
                    @ApiResponse(responseCode = "409", description = "Conflit d'horaire")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('INFIRMIER', 'MEDECIN', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> createAppointment(@Valid @RequestBody CreateAppointmentDTO dto) {
        logger.info("Requête de création d'un nouveau rendez-vous reçue");

        try {
            AppointmentResponseDTO createdAppointment = appointmentService.createAppointment(dto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Rendez-vous créé avec succès");
            response.put("appointment", createdAppointment);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ValidationException e) {
            logger.warn("Erreur de validation lors de la création du rendez-vous: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (ResourceNotFoundException e) {
            logger.warn("Ressource non trouvée: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la création du rendez-vous", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Une erreur est survenue lors de la création du rendez-vous");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Récupère un rendez-vous par son ID
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Récupérer un rendez-vous par ID",
            description = "Permet de récupérer les détails d'un rendez-vous spécifique",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Rendez-vous trouvé",
                            content = @Content(schema = @Schema(implementation = AppointmentResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Rendez-vous non trouvé")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('INFIRMIER', 'MEDECIN', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getAppointmentById(
            @Parameter(description = "ID du rendez-vous", required = true)
            @PathVariable Long id
    ) {
        logger.info("Requête de récupération du rendez-vous ID: {}", id);

        try {
            AppointmentResponseDTO appointment = appointmentService.getAppointmentById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("appointment", appointment);

            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            logger.warn("Rendez-vous non trouvé: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération du rendez-vous", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Une erreur est survenue lors de la récupération du rendez-vous");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Récupère tous les rendez-vous d'un médecin pour une date donnée
     */
    @GetMapping("/doctor/{doctorId}/date/{date}")
    @Operation(
            summary = "Récupérer les rendez-vous d'un médecin par date",
            description = "Permet de récupérer tous les rendez-vous d'un médecin pour une date spécifique",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Liste des rendez-vous récupérée avec succès"),
                    @ApiResponse(responseCode = "404", description = "Médecin non trouvé")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('INFIRMIER', 'MEDECIN', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getDoctorAppointmentsForDate(
            @Parameter(description = "ID du médecin", required = true)
            @PathVariable Long doctorId,

            @Parameter(description = "Date au format yyyy-MM-dd", required = true)
            @PathVariable String date
    ) {
        logger.info("Requête des rendez-vous du médecin {} pour la date {}", doctorId, date);

        try {
            List<AppointmentResponseDTO> appointments = appointmentService.getDoctorAppointmentsForDate(doctorId, date);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", appointments.size());
            response.put("appointments", appointments);

            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            logger.warn("Erreur de validation: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (ResourceNotFoundException e) {
            logger.warn("Ressource non trouvée: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des rendez-vous", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Une erreur est survenue lors de la récupération des rendez-vous");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Met à jour le statut d'un rendez-vous
     */
    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Mettre à jour le statut d'un rendez-vous",
            description = "Permet de modifier le statut d'un rendez-vous existant (PROGRAMMÉ, CONFIRMÉ, ANNULÉ, TERMINÉ, ABSENT)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Statut mis à jour avec succès"),
                    @ApiResponse(responseCode = "400", description = "Statut invalide"),
                    @ApiResponse(responseCode = "404", description = "Rendez-vous non trouvé")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('INFIRMIER', 'MEDECIN', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> updateAppointmentStatus(
            @Parameter(description = "ID du rendez-vous", required = true)
            @PathVariable Long id,

            @Parameter(description = "Nouveau statut", required = true)
            @RequestParam String status
    ) {
        logger.info("Requête de mise à jour du statut du rendez-vous ID {} vers {}", id, status);

        try {
            AppointmentResponseDTO updatedAppointment = appointmentService.updateAppointmentStatus(id, status);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Statut du rendez-vous mis à jour avec succès");
            response.put("appointment", updatedAppointment);

            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            logger.warn("Erreur de validation: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (ResourceNotFoundException e) {
            logger.warn("Rendez-vous non trouvé: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour du statut du rendez-vous", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Une erreur est survenue lors de la mise à jour du statut du rendez-vous");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Supprime un rendez-vous
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Supprimer un rendez-vous",
            description = "Permet de supprimer un rendez-vous existant",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Rendez-vous supprimé avec succès"),
                    @ApiResponse(responseCode = "404", description = "Rendez-vous non trouvé")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('MEDECIN', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteAppointment(
            @Parameter(description = "ID du rendez-vous", required = true)
            @PathVariable Long id
    ) {
        logger.info("Requête de suppression du rendez-vous ID: {}", id);

        try {
            appointmentService.deleteAppointment(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Rendez-vous supprimé avec succès");

            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            logger.warn("Rendez-vous non trouvé: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression du rendez-vous", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Une erreur est survenue lors de la suppression du rendez-vous");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}