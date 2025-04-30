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

    /**
     * Trouve un patient par son numéro de carte d'identité
     */
    Optional<Patient> findByNationalIDcardnumber(String nationalIDcardnumber);

    /**
     * Vérifie si un patient existe avec le numéro de carte d'identité donné
     */
    boolean existsByNationalIDcardnumber(String nationalIDcardnumber);

    /**
     * Recherche des patients par nom ou prénom (insensible à la casse)
     */
    List<Patient> findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCase(String name, String surname);

    /**
     * Trouve des patients nés avant une date donnée
     */
    List<Patient> findByDateofbirthBefore(LocalDate date);

    /**
     * Trouve des patients par groupe sanguin
     */
    List<Patient> findByBloodType(String bloodType);

    /**
     * Recherche avancée de patients
     */
    @Query("SELECT p FROM Patient p WHERE " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:surname IS NULL OR LOWER(p.surname) LIKE LOWER(CONCAT('%', :surname, '%'))) AND " +
            "(:nationalIDcardnumber IS NULL OR p.nationalIDcardnumber = :nationalIDcardnumber) AND " +
            "(:bloodType IS NULL OR p.bloodType = :bloodType)")
    List<Patient> searchPatients(
            @Param("name") String name,
            @Param("surname") String surname,
            @Param("nationalIDcardnumber") String nationalIDcardnumber,
            @Param("bloodType") String bloodType
    );

    /**
     * Trouve des patients avec une allergie spécifique
     */
    @Query("SELECT p FROM Patient p WHERE LOWER(p.allergies) LIKE LOWER(CONCAT('%', :allergie, '%'))")
    List<Patient> findPatientsWithAllergy(@Param("allergie") String allergie);

    /**
     * Compte le nombre de patients par groupe sanguin
     */
    @Query("SELECT p.bloodType, COUNT(p) FROM Patient p GROUP BY p.bloodType")
    List<Object[]> countPatientsByBloodType();

    /**
     * Vérifie si un email est déjà utilisé
     */
    boolean existsByEmail(String email);

    /**
     * Trouve des patients sans rendez-vous récents
     */
    @Query("SELECT p FROM Patient p WHERE p.id NOT IN " +
            "(SELECT a.patient.id FROM AppointementDoctor a WHERE a.date > :lastDate)")
    List<Patient> findPatientsWithNoRecentAppointments(@Param("lastDate") LocalDate lastDate);
}