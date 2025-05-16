package com.example.SGHS4.controller;

import com.example.SGHS4.dto.ConsultationDTO;
import com.example.SGHS4.entite.Consultation;
import com.example.SGHS4.service.ConsultationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/consultations")
@CrossOrigin(origins = "*")
public class ConsultationController {

    @Autowired
    private ConsultationService consultationService;

    @PostMapping
    public ResponseEntity<Consultation> create(@RequestBody ConsultationDTO dto) {
        Consultation saved = consultationService.createConsultation(dto);
        return ResponseEntity.status(201).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<Consultation>> getAll() {
        return ResponseEntity.ok(consultationService.getAllConsultations());
    }
}
