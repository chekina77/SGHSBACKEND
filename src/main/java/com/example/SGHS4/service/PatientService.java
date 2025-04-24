package com.example.SGHS4.service;

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

    public Patient createPatient(Patient patient) {
        return patientRepository.save(patient);
    }

    public Optional<Patient> getPatientById(Long id) {
        return patientRepository.findById(id);
    }

    public Optional<Patient> updatePatient(Long id, Patient patientDetails) {
        return patientRepository.findById(id).map(existingPatient -> {
            existingPatient.setName(patientDetails.getName());
            existingPatient.setAge(patientDetails.getAge());
            existingPatient.setPhoneNumber(patientDetails.getPhoneNumber());
            return patientRepository.save(existingPatient);
        });
    }
    public List<Patient> searchPatients(String keyword) {
        return patientRepository.searchByKeyword(keyword);
    }

}
