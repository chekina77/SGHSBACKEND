package com.example.SGHS4.controller;

import com.example.SGHS4.dto.AppointmentDTO;
import com.example.SGHS4.dto.AppointmentResponseDTO;
import com.example.SGHS4.dto.CreateAppointmentDTO;
import com.example.SGHS4.service.AppointmentService;
import com.example.SGHS4.service.AppointementDoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointementDoctorService appointementDoctorService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService,
                                 AppointementDoctorService appointementDoctorService) {
        this.appointmentService = appointmentService;
        this.appointementDoctorService = appointementDoctorService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<AppointmentDTO>> getAll() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> create(@RequestBody CreateAppointmentDTO dto) {
        AppointmentResponseDTO created = appointementDoctorService.createAppointmentUsingUtilisateur(dto);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping("/enregistrements/rendezvous")
    public ResponseEntity<List<AppointmentDTO>> getRendezvous() {
        return ResponseEntity.ok(appointmentService.getRendezvous());
    }

    @GetMapping("/search")
    public ResponseEntity<List<AppointmentResponseDTO>> searchAppointments(@RequestParam("keyword") String keyword) {
        return ResponseEntity.ok(appointmentService.searchAppointments(keyword));
    }

    @PutMapping("/{id}/statut")
    public ResponseEntity<Void> updateStatut(@PathVariable Long id, @RequestParam("statut") String statut) {
        appointmentService.updateAppointmentStatut(id, statut);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAppointment(@PathVariable Long id) {
        try {
            appointmentService.deleteAppointment(id);
            return ResponseEntity.ok("Rendez-vous supprimé avec succès.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}
