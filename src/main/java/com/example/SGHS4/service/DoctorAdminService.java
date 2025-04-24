package com.example.SGHS4.service;

import com.example.SGHS4.entite.Doctor;
import com.example.SGHS4.entite.Patient;
import com.example.SGHS4.repository.DoctorAdminRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorAdminService {
    private final DoctorAdminRepository doctorAdminRepository;

    public DoctorAdminService(DoctorAdminRepository doctorAdminRepository) {
        this.doctorAdminRepository = doctorAdminRepository;
    }

    // Obtenir tous les docteurs
    public List<Doctor> getAllDoctors() {
        return doctorAdminRepository.findAll();
    }

    // Créer un nouveau docteur
    public Doctor createDoctor(Doctor doctor) {
        return doctorAdminRepository.save(doctor);
    }

    // Récupérer un docteur par ID
    public Optional<Doctor> getDoctorById(Long id) {
        return doctorAdminRepository.findById(id);
    }

    // Mettre à jour les informations d’un docteur
    public Optional<Doctor> updateDoctor(Long id, Doctor doctorDetails) {
        return doctorAdminRepository.findById(id).map(existingDoctor -> {
            existingDoctor.setName(doctorDetails.getName());
            existingDoctor.setSpecialization(doctorDetails.getSpecialization());
            existingDoctor.setPhoneNumber(doctorDetails.getPhoneNumber());
            return doctorAdminRepository.save(existingDoctor);
        });
    }
    public List<Doctor> searchDoctors(String keyword) {
        return DoctorAdminRepository.searchByKeyword(keyword);
    }
}
