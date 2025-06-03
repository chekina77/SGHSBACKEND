package com.example.SGHS4.controller;

import com.example.SGHS4.dto.LivretDTO;
import com.example.SGHS4.entite.Livret;
import com.example.SGHS4.service.LivretService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/livret")
public class LivretController {

    @Autowired
    private LivretService livretService;

    @PostMapping("/create")
    public ResponseEntity<?> createOrUpdateLivret(@RequestBody LivretDTO livretDTO) {
        try {
            Livret livret = livretService.createLivret(livretDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(new LivretDTO(livret));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Pour un patient connecté

    // ✅ Pour MEDECIN ou ADMINISTRATEUR connecté

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getLivretsForPatient(@PathVariable Long patientId) {
        try {
            List<LivretDTO> dtos = livretService.getLivretsForPatient(patientId);
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/patient")
    public ResponseEntity<?> getLivretsForAuthenticatedPatient() {
        try {
            List<LivretDTO> dtos = livretService.getLivretsForAuthenticatedPatient();
            return ResponseEntity.ok(dtos);
        } catch (AccessDeniedException ade) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ade.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/{patientId}")
    public ResponseEntity<LivretDTO> getLivretByPatient(@PathVariable Long patientId) {
        try {
            LivretDTO dto = livretService.getLivretByPatientId(patientId);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


}
