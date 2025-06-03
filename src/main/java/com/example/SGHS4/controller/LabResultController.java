package com.example.SGHS4.controller;

import com.example.SGHS4.dto.LabResultDTO;
import com.example.SGHS4.service.LabResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/labresults")
public class LabResultController {

    @Autowired
    private LabResultService labResultService;


    // Créer un nouveau résultat de laboratoire
    @PostMapping
    public ResponseEntity<LabResultDTO> createLabResult(@RequestBody LabResultDTO labResultDTO) {
        try {
            LabResultDTO savedDTO = labResultService.createAndReturnDTO(labResultDTO);
            return ResponseEntity.ok(savedDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Récupérer un résultat de laboratoire par son ID (déchiffré)
    @GetMapping("/{id}")
    public ResponseEntity<LabResultDTO> getLabResultById(@PathVariable Long id) {
        try {
            LabResultDTO dto = labResultService.getLabResultById(id);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Récupérer tous les résultats d’un patient par patientId (déchiffrés)
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<LabResultDTO>> getAllLabResultsByPatient(@PathVariable Long patientId) {
        try {
            List<LabResultDTO> results = labResultService.getAllLabResultsDecrypted(patientId);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            e.printStackTrace();  // Pour voir l’erreur dans les logs serveur
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Optionnel: supprimer un résultat
    /*@DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLabResult(@PathVariable Long id) {
        try {
            labResultService.deleteLabResult(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }*/

}
