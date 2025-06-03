package com.example.SGHS4.repository;

import com.example.SGHS4.entite.AppointementDoctor;
import com.example.SGHS4.entite.Doctor;
import com.example.SGHS4.entite.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface AppointementDoctorRepository extends JpaRepository<AppointementDoctor, Long> {

        List<AppointementDoctor> findByUtilisateurAndAppointmentDateBetween(Utilisateur utilisateur, LocalDateTime start, LocalDateTime end);

    List<AppointementDoctor> findByPatientNameContainingIgnoreCaseOrMedecinNameContainingIgnoreCase(String patientName, String medecinName);
    List<AppointementDoctor> findByUtilisateur(Utilisateur utilisateur); // selon votre besoin




    // Recherche par médecin et plage de date
}
