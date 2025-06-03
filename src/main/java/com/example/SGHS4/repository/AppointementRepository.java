package com.example.SGHS4.repository;

import com.example.SGHS4.entite.Appointment;
import com.example.SGHS4.entite.Appointment;
import com.example.SGHS4.entite.Patient;
import com.example.SGHS4.entite.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointementRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findAllByOrderByAppointmentDateDesc();
    @Query
            ("SELECT a FROM Appointment a WHERE LOWER(a.patient) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(a.doctor) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Appointment> searchByKeyword(String keyword);

}
