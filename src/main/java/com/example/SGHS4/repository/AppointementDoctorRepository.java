package com.example.SGHS4.repository;

import com.example.SGHS4.entite.AppointementDoctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointementDoctorRepository extends JpaRepository<AppointementDoctor, Long> {
    // Recherche par nom contenant le mot-clé (insensible à la casse)
    List<AppointementDoctor> findByNameContainingIgnoreCase(String keyword);
}
