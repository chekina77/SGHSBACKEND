package com.example.SGHS4.controller;

import com.example.SGHS4.dto.PatientDTO;
import com.example.SGHS4.service.PatientService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patient")
public class PatientConntroller {

    private final PatientService patientService;

    public PatientConntroller(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping("/create")
    public void createPatient(PatientDTO patientDTO){
        patientService.createPatient(patientDTO);
    }
}
