package com.example.SGHS4.repository;

import com.example.SGHS4.entite.AppointementDoctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointementDoctorRepository extends JpaRepository<AppointementDoctor, Long> {

    // Trouver tous les rendez-vous d'un médecin spécifique
    List<AppointementDoctor> findByDoctorId(Long doctorId);

    // Trouver les rendez-vous par date (exemple supplémentaire)
    List<AppointementDoctor> findByDate(LocalDateTime date);

    // Trouver les rendez-vous d'un patient spécifique (si besoin)
    List<AppointementDoctor> findByPatientId(Long patientId);

    // Trouver les rendez-vous futurs d'un médecin
    List<AppointementDoctor> findByDoctorIdAndDateAfter(Long doctorId, LocalDateTime date);

    // Vérifier l'existence d'un rendez-vous à une date/heure pour un médecin
    boolean existsByDoctorIdAndDate(Long doctorId, LocalDateTime date);
}