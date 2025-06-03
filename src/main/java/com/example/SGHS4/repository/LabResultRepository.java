package com.example.SGHS4.repository;

import com.example.SGHS4.entite.LabResult;
import com.example.SGHS4.entite.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabResultRepository extends JpaRepository<LabResult, Long> {
    List<LabResult> findByPatientOrderByCreatedAtDesc(Patient patient);
}
