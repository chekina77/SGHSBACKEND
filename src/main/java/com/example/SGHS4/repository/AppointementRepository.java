package com.example.SGHS4.repository;

import com.example.SGHS4.entite.Appointement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointementRepository extends JpaRepository<Appointement, Long> {
    List<Appointement> findAllByOrderByDateDesc();
    @Query
            ("SELECT a FROM Appointement a WHERE LOWER(a.PatientName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(a.DoctorName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Appointement> searchByKeyword(String keyword);

}
