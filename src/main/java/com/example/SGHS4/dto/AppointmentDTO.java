package com.example.SGHS4.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class AppointmentDTO {
    private Long id;
    private Long patientId;
    private Long doctorId;

    @JsonProperty("name")
    private String patientName;

    private String doctor;
    private String status;

    @JsonProperty("lastVisit")
    private LocalDateTime appointmentDate;

    public AppointmentDTO() {}

    public AppointmentDTO(Long id, String patientName, String doctor, String status, LocalDateTime appointmentDate) {
        this.id = id;
        this.patientName = patientName;
        this.doctor = doctor;
        this.status = status;
        this.appointmentDate = appointmentDate;
    }

    // Pour getRendezvous()
    public AppointmentDTO(Long id, String patientName, LocalDateTime appointmentDate) {
        this.id = id;
        this.patientName = patientName;
        this.appointmentDate = appointmentDate;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getDoctor() { return doctor; }
    public void setDoctor(String doctor) { this.doctor = doctor; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDateTime appointmentDate) { this.appointmentDate = appointmentDate; }
}
