package com.example.SGHS4.repository;

import com.example.SGHS4.entite.Appointement;
import com.example.SGHS4.entite.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    @Query("SELECT p FROM Patient p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "CAST(p.age AS string) LIKE CONCAT('%', :keyword, '%') OR " +
            "p.phoneNumber LIKE CONCAT('%', :keyword, '%')")
    List<Patient> searchByKeyword(@Param("keyword") String keyword);


}
