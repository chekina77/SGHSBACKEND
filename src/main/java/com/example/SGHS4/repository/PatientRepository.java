package com.example.SGHS4.repository;

import com.example.SGHS4.entite.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByFingerprintHash(String fingerprintHash);


    // Trouve un patient par son numéro de carte d'identité
    Optional<Patient> findByNationalIDcardnumber(String nationalIDcardnumber);

    // Vérifie si un patient existe avec le numéro de carte d'identité donné
    boolean existsByNationalIDcardnumber(String nationalIDcardnumber);

    // Recherche des patients par nom ou prénom (insensible à la casse)
    List<Patient> findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCase(String name, String surname);
    Optional<Patient> findByNameAndSurname(String name, String surname);


    // Trouve des patients nés avant une date donnée
    List<Patient> findByDateOfBirthBefore(LocalDate date);

    // Recherche avancée de patients
    @Query("SELECT p FROM Patient p WHERE " +
            "(:nom IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :nom, '%'))) AND " +
            "(:prenom IS NULL OR LOWER(p.surname) LIKE LOWER(CONCAT('%', :prenom, '%'))) AND " +
            "(:nationalIDcardnumber IS NULL OR p.nationalIDcardnumber = :nationalIDcardnumber)")
    List<Patient> searchPatients(
            @Param("nom") String nom,
            @Param("prenom") String prenom,
            @Param("nationalIDcardnumber") String nationalIDcardnumber
    );

    // Trouve des patients avec une allergie spécifique
    @Query("SELECT p FROM Patient p WHERE LOWER(p.allergies) LIKE LOWER(CONCAT('%', :allergie, '%'))")
    List<Patient> findPatientsWithAllergy(@Param("allergie") String allergie);

    // Vérifie si un email est déjà utilisé
    boolean existsByEmail(String email);

    // Trouve des patients sans rendez-vous récents
    @Query("SELECT p FROM Patient p WHERE p.id NOT IN " +
            "(SELECT a.id FROM AppointementDoctor a WHERE a.appointmentDate > :lastDate)")
    List<Patient> findPatientsWithNoRecentAppointments(@Param("lastDate") LocalDate lastDate);
}
