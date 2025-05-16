
package com.example.SGHS4.controller;

import com.example.SGHS4.dto.*;
import com.example.SGHS4.entite.Doctor;
import com.example.SGHS4.service.AppointementDoctorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller unique pour gérer les rendez‑vous et la liste des médecins.
 */
@RestController
@RequestMapping("/api/doctor")
@Validated
public class AppointementDoctorController {

    private AppointementDoctorService service;

    @Autowired
    public void AppointmentController(AppointementDoctorService service) {
        this.service = service;
    }

    public AppointementDoctorController(AppointementDoctorService service) {
        this.service = service;
    }

    //---------- Création et gestion des RDV ----------

    /** Créer un patient + rendez‑vous */

    /** Récupérer un RDV par ID */
    @GetMapping("/appointments/{id}")
    public ResponseEntity<Map<String,Object>> getAppointmentById(@PathVariable Long id) {
        Map<String,Object> resp = new HashMap<>();
        try {
            AppointmentResponseDTO dto = service.getAppointmentById(id);
            resp.put("success", true);
            resp.put("data", dto);
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            resp.put("success", false);
            resp.put("message", "Erreur récupération : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    /** Lister tous les RDV (vue admin) */
    @GetMapping("/appointments/all")
    public ResponseEntity<Map<String,Object>> listAllAppointments() {
        Map<String,Object> resp = new HashMap<>();
        try {
            List<AppointmentResponseDTO> list = service.getAllAppointments();
            resp.put("success", true);
            resp.put("data", list); // <== le frontend attend data.data
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            resp.put("success", false);
            resp.put("message", "Erreur récupération : " + ex.getMessage());
            resp.put("data", new ArrayList<>()); // <== éviter un champ "data" absent côté client
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    /** Recherche de RDV (admin) */
    @GetMapping("/appointments/search")
    public ResponseEntity<Map<String,Object>> searchAppointments(@RequestParam String keyword) {
        Map<String,Object> resp = new HashMap<>();
        try {
            List<AppointmentResponseDTO> results = service.searchAppointments(keyword);
            resp.put("success", true);
            resp.put("data", results); // pour correspondre au frontend
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            resp.put("success", false);
            resp.put("message", "Erreur recherche : " + ex.getMessage());
            resp.put("data", new ArrayList<>()); // toujours un champ "data"
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }


    /** RDV d’un médecin pour une date */
  /*  @GetMapping("/appointments/doctor/{doctorId}/date")
    public ResponseEntity<Map<String,Object>> getDoctorAppointmentsForDate(
            @PathVariable Long doctorId,
            @RequestParam String date) {
        Map<String,Object> resp = new HashMap<>();
        try {
            List<AppointmentResponseDTO> list = service.getDoctorAppointmentsForDate(doctorId, LocalDate.parse(date));
            resp.put("success", true);
            resp.put("data", list);
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            resp.put("success", false);
            resp.put("message", "Erreur récupération : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }*/

    /** Supprimer un RDV */
    @DeleteMapping("/appointments/{id}")
    public ResponseEntity<Map<String,Object>> deleteAppointment(@PathVariable Long id) {
        Map<String,Object> resp = new HashMap<>();
        try {
            service.deleteAppointment(id);
            resp.put("success", true);
            resp.put("message", "Rendez-vous supprimé");
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            resp.put("success", false);
            resp.put("message", "Erreur suppression : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    //---------- Liste des médecins pour le front ----------

    /** Retourne la liste des médecins */
    @GetMapping("/doctors")
    public List<Doctor> listDoctors() {
        return service.listDoctors();
    }
}

