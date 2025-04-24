package com.example.SGHS4.controller;

import com.example.SGHS4.entite.PatientDoctor;
import com.example.SGHS4.service.PatientDoctorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/patient-doctors")
public class PatientDoctorController {

    private final PatientDoctorService patientDoctorService;

    public PatientDoctorController(PatientDoctorService patientDoctorService) {
        this.patientDoctorService = patientDoctorService;
    }

    @GetMapping
    public ResponseEntity<List<PatientDoctor>> getAllPatientDoctors() {
        List<PatientDoctor> patientList = patientDoctorService.getAllPatientDoctors();
        return new ResponseEntity<>(patientList, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<PatientDoctor> createPatientDoctor(@RequestBody PatientDoctor patientDoctor) {
        PatientDoctor doctorCreated = patientDoctorService.createPatientDoctor(patientDoctor);
        return new ResponseEntity<>(doctorCreated, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientDoctor> getPatientDoctorById(@PathVariable Long id) {
        Optional<PatientDoctor> patientDoctor = patientDoctorService.getPatientDoctorById(id);
        return patientDoctor
                .map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientDoctor> updatePatientDoctor(@PathVariable Long id, @RequestBody PatientDoctor patientDoctorDetails) {
        Optional<PatientDoctor> updatedPatientDoctor = patientDoctorService.updatePatientDoctor(id, patientDoctorDetails);
        return updatedPatientDoctor
                .map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/search")
    public ResponseEntity<List<PatientDoctor>> searchPatientDoctors(@RequestParam String keyword) {
        List<PatientDoctor> result = patientDoctorService.searchPatientDoctors(keyword);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
