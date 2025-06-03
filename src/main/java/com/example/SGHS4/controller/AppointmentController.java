package com.example.SGHS4.controller;

import com.example.SGHS4.dto.AppointmentDTO;
import com.example.SGHS4.dto.AppointmentResponseDTO;
import com.example.SGHS4.dto.CreateAppointmentDTO;
import com.example.SGHS4.dto.PatientDTO;
import com.example.SGHS4.entite.AppointementDoctor;
import com.example.SGHS4.repository.PatientRepository;
import com.example.SGHS4.service.AppointmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final Logger log = LoggerFactory.getLogger(AppointmentController.class);

    @Autowired
    public AppointmentController(AppointmentService appointmentService, PatientRepository patientRepository) {
        this.appointmentService = appointmentService;
    }

    // ✅ Créer un rendez-vous (enregistre aussi le patient si nouveau)
    @PostMapping("/appointment-utilisateur")
    public ResponseEntity<?> createWithUtilisateur(
            @RequestBody CreateAppointmentDTO dto,
            Principal principal) {
        try {
            String username = principal.getName(); // récupère l'email de l'utilisateur connecté
            AppointmentResponseDTO response = appointmentService.createAppointmentUsingUtilisateur(dto, username);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Erreurs métier (ex : médecin non trouvé, utilisateur non trouvé, chiffrement, etc.)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("status", "error", "message", e.getMessage())
            );
        } catch (Exception e) {
            // Erreurs serveur inattendues
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("status", "error", "message", "Erreur serveur : " + e.getMessage())
            );
        }
    }

    @GetMapping("/{id}/decrypted")
    public PatientDTO getDecryptedPatientInfo(@PathVariable Long id) {
        return appointmentService.getPatientDecryptedInfo(id);
    }
    // ✅ Obtenir tous les rendez-vous (à adapter si getAllAppointments() est implémenté dans le service)
    @GetMapping("/all")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments() {
        try {
            List<AppointmentDTO> list = appointmentService.getAllAppointments();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des rendez-vous", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ Rechercher des rendez-vous par mot-clé (si la méthode est implémentée)
    @GetMapping("/search")
    public ResponseEntity<List<AppointmentResponseDTO>> searchAppointments(@RequestParam("keyword") String keyword) {
        try {
            List<AppointmentResponseDTO> results = appointmentService.searchAppointments(keyword);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Erreur recherche rendez-vous : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ Mise à jour du statut
    @PutMapping("/{id}/statut")
    public ResponseEntity<Void> updateStatut(@PathVariable Long id, @RequestParam("statut") String statut) {
        try {
            appointmentService.updateAppointmentStatut(id, statut);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("Erreur mise à jour du statut : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // ✅ Suppression d’un rendez-vous
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAppointment(@PathVariable Long id) {
        try {
            appointmentService.deleteAppointment(id);
            return ResponseEntity.ok("Rendez-vous supprimé avec succès.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ✅ Déchiffrer les patients à partir d’une CNI
    @GetMapping("/all-Patients")
    public ResponseEntity<List<PatientDTO>> getAllPatients() {
        List<PatientDTO> patients = appointmentService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

}
