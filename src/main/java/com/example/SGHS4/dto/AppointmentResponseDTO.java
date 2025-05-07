package com.example.SGHS4.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppointmentResponseDTO {

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;

    private String status;               // ajouté pour refléter le statut
    private DoctorDTO doctor;
    private PatientDTO patient;

    public AppointmentResponseDTO() {
    }

    public AppointmentResponseDTO(Long id, LocalDateTime date, String status, DoctorDTO doctor, PatientDTO patient) {
        this.id = id;
        this.date = date;
        this.status = status;
        this.doctor = doctor;
        this.patient = patient;
    }

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public DoctorDTO getDoctor() { return doctor; }
    public void setDoctor(DoctorDTO doctor) { this.doctor = doctor; }

    public PatientDTO getPatient() { return patient; }
    public void setPatient(PatientDTO patient) { this.patient = patient; }
}