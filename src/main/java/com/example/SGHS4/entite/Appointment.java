package com.example.SGHS4.entite;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Date;

@Entity
public class Appointment {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Patient patient;
    @ManyToOne private Doctor doctor;
    private LocalDate appointmentDate;
    private String status; // "alive" or "dead"

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    // getters/setters
}