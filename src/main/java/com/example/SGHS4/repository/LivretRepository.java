package com.example.SGHS4.repository;

import com.example.SGHS4.entite.Livret;
import com.example.SGHS4.entite.Patient;
import com.example.SGHS4.entite.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LivretRepository extends JpaRepository<Livret, Long> {
    // Méthode pour trouver un livret par le patient
    Optional<Livret> findByPatient(Patient patient);
    List<Livret> findByPatientOrderByConsultationDateDesc(Patient patient);
    Optional<Livret> findByPatientId(Long patientId);




    List<Livret> findByDoctor(Utilisateur utilisateur);
}
