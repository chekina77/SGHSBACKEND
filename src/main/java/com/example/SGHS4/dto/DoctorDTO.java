package com.example.SGHS4.dto;

import java.time.LocalDateTime;

public class DoctorDTO {
    private Long id;
    private String patientName;
    private LocalDateTime appointmentDate;
    private Long doctorId;

    // Constructeur par défaut
    public DoctorDTO() {
    }

    // Constructeur avec paramètres
    public DoctorDTO(Long id, String patientName, LocalDateTime appointmentDate, Long doctorId) {
        this.id = id;
        this.patientName = patientName;
        this.appointmentDate = appointmentDate;
        this.doctorId = doctorId;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public LocalDateTime getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDateTime appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
}
}