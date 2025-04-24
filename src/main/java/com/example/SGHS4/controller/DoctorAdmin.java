package com.example.SGHS4.controller;

import com.example.SGHS4.entite.Doctor;
import com.example.SGHS4.entite.Patient;
import com.example.SGHS4.service.DoctorAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/doctors")
public class DoctorAdmin {

    private final DoctorAdminService doctorAdminService;


    public DoctorAdmin(DoctorAdminService doctorAdminService) {
        this.doctorAdminService = doctorAdminService;
    }

    // Récupérer tous les docteurs
    @GetMapping
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        List<Doctor> doctors = doctorAdminService.getAllDoctors();
        return new ResponseEntity<>(doctors, HttpStatus.OK);
    }

    // Créer un docteur
    @PostMapping
    public ResponseEntity<Doctor> createDoctor(@RequestBody Doctor doctor) {
        Doctor doctorCreated = doctorAdminService.createDoctor(doctor);
        return new ResponseEntity<>(doctorCreated, HttpStatus.CREATED);
    }

    // Récupérer un docteur par ID
    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable Long id) {
        Optional<Doctor> doctor = doctorAdminService.getDoctorById(id);
        return doctor.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Modifier les informations d’un docteur
    @PutMapping("/{id}")
    public ResponseEntity<Doctor> updateDoctor(@PathVariable Long id, @RequestBody Doctor doctorDetails) {
        Optional<Doctor> updatedDoctor = doctorAdminService.updateDoctor(id, doctorDetails);
        return updatedDoctor.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    @GetMapping("/search")
    public ResponseEntity<List<Doctor>> searchDoctors(@RequestParam String keyword) {
        List<Doctor> result = doctorAdminService.searchDoctors(keyword);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
