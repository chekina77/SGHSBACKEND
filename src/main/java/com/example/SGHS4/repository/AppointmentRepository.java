package com.example.SGHS4.repository;

import com.example.SGHS4.entite.AppointementDoctor;
import com.example.SGHS4.entite.Appointment;
import com.example.SGHS4.entite.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {


}