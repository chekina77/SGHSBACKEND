package com.example.SGHS4.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppointmentResponseDTO {

    private Long id;
    private String patientName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime appointmentDate;

    private String medecinName;
    private String statut;

    public AppointmentResponseDTO() {}

    public AppointmentResponseDTO(Long id, String patientName, LocalDateTime appointmentDate, String medecinName, String statut) {
        this.id = id;
        this.patientName = patientName;
        this.appointmentDate = appointmentDate;
        this.medecinName = medecinName;
        this.statut = statut;
    }

    public AppointmentResponseDTO(Long id, String patientName, LocalDate appointmentDate, String medecinName, String statut) {
        this.id = id;
        this.patientName = patientName;
        this.appointmentDate = appointmentDate.atStartOfDay(); // conversion de LocalDate vers LocalDateTime
        this.medecinName = medecinName;
        this.statut = statut;
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

    public String getMedecinName() {
        return medecinName;
    }

    public void setMedecinName(String medecinName) {
        this.medecinName = medecinName;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }
}
