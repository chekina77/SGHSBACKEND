package com.example.SGHS4.controller;

import com.example.SGHS4.dto.PatientDTO;
import com.example.SGHS4.service.PatientService;
import com.example.SGHS4.exceptions.ResourceNotFoundException;
import com.example.SGHS4.exceptions.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    private final PatientService patientService;

    @Autowired
    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    /**
     * Crée un nouveau patient
     * @param patientDTO Les données du patient à créer
     * @return Le patient créé
     */
    @PostMapping
    public ResponseEntity<PatientDTO> createPatient(@RequestBody PatientDTO patientDTO) {
        try {
            PatientDTO createdPatient = patientService.createPatient(patientDTO);
            return new ResponseEntity<>(createdPatient, HttpStatus.CREATED);
        } catch (ValidationException ex) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Récupère un patient par son ID
     * @param id L'ID du patient
     * @return Le patient trouvé
     */
    @GetMapping("/{id}")
    public ResponseEntity<PatientDTO> getPatientById(@PathVariable Long id) {
        try {
            PatientDTO patientDTO = patientService.getPatientById(id);
            return new ResponseEntity<>(patientDTO, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Récupère tous les patients
     * @return Liste de tous les patients
     */
    @GetMapping
    public ResponseEntity<List<PatientDTO>> getAllPatients() {
        List<PatientDTO> patients = patientService.getAllPatients();
        return new ResponseEntity<>(patients, HttpStatus.OK);
    }

    /**
     * Met à jour un patient
     * @param id L'ID du patient à mettre à jour
     * @param patientDTO Les nouvelles données du patient
     * @return Le patient mis à jour
     */
    @PutMapping("/{id}")
    public ResponseEntity<PatientDTO> updatePatient(@PathVariable Long id, @RequestBody PatientDTO patientDTO) {
        try {
            PatientDTO updatedPatient = patientService.updatePatient(id, patientDTO);
            return new ResponseEntity<>(updatedPatient, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (ValidationException ex) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Supprime un patient
     * @param id L'ID du patient à supprimer
     * @return Status de la suppression
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        try {
            patientService.deletePatient(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Recherche des patients par nom ou prénom
     * @param query Terme de recherche
     * @return Liste des patients correspondants
     */
    @GetMapping("/search")
    public ResponseEntity<List<PatientDTO>> searchPatients(@RequestParam String query) {
        List<PatientDTO> patients = patientService.searchPatients(query);
        return new ResponseEntity<>(patients, HttpStatus.OK);
    }

    /**
     * Recherche un patient par son numéro de carte d'identité
     * @param nationalIdCard Numéro de carte d'identité
     * @return Le patient trouvé ou un statut non trouvé
     */
    @GetMapping("/search/identifiant")
    public ResponseEntity<PatientDTO> findByNationalIdCard(@RequestParam String nationalIdCard) {
        PatientDTO patientDTO = patientService.findByNationalIdCard(nationalIdCard);
        if (patientDTO != null) {
            return new ResponseEntity<>(patientDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}
