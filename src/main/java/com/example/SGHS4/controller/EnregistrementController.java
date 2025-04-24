package com.example.SGHS4.controller;

import com.example.SGHS4.entite.Enregistrement;
import com.example.SGHS4.service.EnregistrementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/enregistrements")
@CrossOrigin("http://localhost:5173/")
public class EnregistrementController {

    private final EnregistrementService enregistrementService;

    public EnregistrementController(EnregistrementService enregistrementService) {
        this.enregistrementService = enregistrementService;
    }

    @PostMapping
    public ResponseEntity<Enregistrement> enregistrerPatient(@RequestBody Enregistrement enregistrement) {
        Enregistrement saved = enregistrementService.enregistrerPatient(enregistrement);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<Enregistrement>> getAll() {
        return ResponseEntity.ok(enregistrementService.getAllEnregistrements());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Enregistrement> getById(@PathVariable Long id) {
        return enregistrementService.getEnregistrementById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
