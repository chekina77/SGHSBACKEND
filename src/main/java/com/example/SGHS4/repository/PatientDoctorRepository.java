package com.example.SGHS4.repository;

import com.example.SGHS4.entite.PatientDoctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientDoctorRepository extends JpaRepository<PatientDoctor, Long> {

    @Query("SELECT p FROM PatientDoctor p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "CAST(p.age AS string) LIKE %:keyword% OR " +
            "LOWER(p.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.lastvisit) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<PatientDoctor> searchByKeyword(String keyword);
}
