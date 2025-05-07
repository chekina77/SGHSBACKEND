package com.example.SGHS4.repository;

import com.example.SGHS4.entite.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Interface de persistance JPA pour les médecins.
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    /**
     * Recherche des médecins dont le nom ou le prénom contient la chaîne donnée (insensible à la casse).
     */
    List<Doctor> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(String nom, String prenom);
}