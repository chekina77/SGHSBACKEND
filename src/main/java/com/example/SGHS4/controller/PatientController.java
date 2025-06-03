package com.example.SGHS4.controller;

import com.example.SGHS4.dto.PatientDTO;
import com.example.SGHS4.exceptions.ResourceNotFoundException;
import com.example.SGHS4.exceptions.ValidationException;
import com.example.SGHS4.service.AppointmentService;
import com.example.SGHS4.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    private final PatientService patientService;
    private final AppointmentService appointmentService;

    @Autowired
    public PatientController(PatientService patientService, AppointmentService appointmentService) {
        this.patientService = patientService;
        this.appointmentService = appointmentService;
    }

    @PostMapping
    @Operation(summary = "Créer un nouveau patient")
    public ResponseEntity<PatientDTO> createPatient(@RequestBody PatientDTO patientDTO) {
        try {
            PatientDTO created = patientService.createPatient(patientDTO);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (ValidationException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un patient par son ID")
    public ResponseEntity<PatientDTO> getPatientById(@PathVariable Long id) {
        try {
            PatientDTO patient = patientService.getPatientById(id);  // Plus de cniKey
            return new ResponseEntity<>(patient, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/all")
    @Operation(summary = "Obtenir tous les patients")
    public ResponseEntity<List<PatientDTO>> getAllPatients() {
        try {
            List<PatientDTO> patients = patientService.getAllPatients(); // Plus de cniKey
            return new ResponseEntity<>(patients, HttpStatus.OK);
        } catch (ValidationException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un patient")
    public ResponseEntity<PatientDTO> updatePatient(
            @PathVariable Long id,
            @RequestBody PatientDTO patientDTO) {
        try {
            PatientDTO updated = patientService.updatePatient(id, patientDTO);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (ValidationException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un patient par ID")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        try {
            patientService.deletePatient(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des patients")
    public ResponseEntity<List<PatientDTO>> searchPatients(@RequestParam String query) {
        try {
            List<PatientDTO> results = patientService.searchPatients(query);
            return new ResponseEntity<>(results, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/findByCNI")
    @Operation(summary = "Rechercher un patient par numéro de carte d'identité")
    public ResponseEntity<PatientDTO> findByNationalIdCard(@RequestParam String cni) {
        try {
            PatientDTO patient = patientService.findByNationalIdCard(cni);
            if (patient == null) {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(patient, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/decrypted-names")
    public List<PatientDTO> getAllPatientsNameAndSurnameDecrypted() {
        return patientService.getAllPatientsNameAndSurnameDecrypted();
    }
    }

