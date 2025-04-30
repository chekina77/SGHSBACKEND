package com.example.SGHS4.service;

import com.example.SGHS4.dto.PatientDTO;
import com.example.SGHS4.entite.Patient;
import com.example.SGHS4.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public void createPatient(PatientDTO patientDTO) {


            Patient patient = new Patient();

            patient.setName(patientDTO.getName());
            patient.setSurname(patientDTO.getSurname());
            patient.setSexe(patientDTO.getSexe());
            patient.setDateofbirth(patientDTO.getDateofbirth());
            patient.setWeight(patientDTO.getWeight());
            patient.setHeight(patientDTO.getHeight());
            patient.setEmail(patientDTO.getEmail());
            patient.setNationalIDcardnumber(patientDTO.getNationalIDcardnumber());
            patient.setComment(patientDTO.getComment());
            patient.setDateoftoday(patientDTO.getDateoftoday());

            patientRepository.save(patient);


    }

    public Optional<Patient> getPatientById(Long id) {
        return patientRepository.findById(id);
    }



}
