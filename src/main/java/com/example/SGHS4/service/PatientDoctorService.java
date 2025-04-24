package com.example.SGHS4.service;

import com.example.SGHS4.entite.PatientDoctor;
import com.example.SGHS4.repository.PatientDoctorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientDoctorService {

    private final PatientDoctorRepository patientDoctorRepository;

    public PatientDoctorService(PatientDoctorRepository patientDoctorRepository) {
        this.patientDoctorRepository = patientDoctorRepository;
    }

    public List<PatientDoctor> getAllPatientDoctors() {
        return patientDoctorRepository.findAll();
    }

    public PatientDoctor createPatientDoctor(PatientDoctor patientDoctor) {
        return patientDoctorRepository.save(patientDoctor);
    }

    public Optional<PatientDoctor> getPatientDoctorById(Long id) {
        return patientDoctorRepository.findById(id);
    }

    public Optional<PatientDoctor> updatePatientDoctor(Long id, PatientDoctor details) {
        return patientDoctorRepository.findById(id).map(existing -> {
            existing.setName(details.getName());
            existing.setAge(details.getAge());
            existing.setPhoneNumber(details.getPhoneNumber());
            existing.setLastvisit(details.getLastvisit());
            return patientDoctorRepository.save(existing);
        });
    }

    public List<PatientDoctor> searchPatientDoctors(String keyword) {
        return patientDoctorRepository.searchByKeyword(keyword);
    }
}
