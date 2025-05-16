package com.example.SGHS4.controller;

import com.example.SGHS4.dto.DoctorDTO;
import com.example.SGHS4.service.DoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST pour gérer les médecins.
 */
@RestController
@RequestMapping("/doctors")
public class DoctorController {

    private final DoctorService service;

    public DoctorController(DoctorService service) {
        this.service = service;
    }

    /**
     * GET /doctors : retourne la liste de tous les médecins.
     */
    @GetMapping
    public ResponseEntity<List<DoctorDTO>> getAll() {
        List<DoctorDTO> doctors = service.getAllDoctors();
        return ResponseEntity.ok(doctors);
    }

    /**
     * POST /doctors : crée un nouveau médecin.
     */
    @PostMapping
    public ResponseEntity<DoctorDTO> create(@RequestBody DoctorDTO dto) {
        DoctorDTO created = service.createDoctor(dto);
        return ResponseEntity.status(201).body(created);
    }

    /**
     * GET /doctors/{id} : récupère un médecin par son ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DoctorDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getDoctorById(id));
    }

    /**
     * PUT /doctors/{id} : met à jour un médecin existant.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DoctorDTO> update(@PathVariable Long id, @RequestBody DoctorDTO dto) {
        return ResponseEntity.ok(service.updateDoctor(id, dto));
    }

    /**
     * DELETE /doctors/{id} : supprime un médecin.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /doctors/search?keyword=... : recherche de médecins par mot‑clé.
     */
    @GetMapping("/search")
    public ResponseEntity<List<DoctorDTO>> search(@RequestParam("keyword") String keyword) {
        return ResponseEntity.ok(service.searchDoctors(keyword));
    }
}