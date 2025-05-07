package com.example.SGHS4.repository;

import com.example.SGHS4.entite.AppointementDoctor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface AppointementDoctorRepository extends JpaRepository<AppointementDoctor, Long> {
    // Recherche par nom du médecin (doctor.nom) ou nom du patient (patient.name)
    List<AppointementDoctor> findByDoctorNomContainingIgnoreCaseOrPatientNameContainingIgnoreCase(
            String doctorNom, String patientName);

    // Recherche par médecin et plage de date
    List<AppointementDoctor> findByDoctorIdAndDateBetween(
            Long doctorId, LocalDateTime start, LocalDateTime end);
}
