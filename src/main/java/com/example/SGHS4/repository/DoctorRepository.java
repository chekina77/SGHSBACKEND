package com.example.SGHS4.repository;

import com.example.SGHS4.entite.Doctor;
import com.example.SGHS4.enums.TypeDeRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Interface de persistance JPA pour les médecins.
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(String nom, String prenom);
    Doctor findFirstByNomIgnoreCase(String nom);

    // Nouvelle méthode pour rechercher par ID et rôle
    Doctor findByIdAndTypeDeRole(Long id, TypeDeRole typeDeRole);


}
