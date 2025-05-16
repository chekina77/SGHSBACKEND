package com.example.SGHS4.dto;

import java.time.LocalDate;

public class ConsultationDTO {

    private Long patientId;
    private String name;
    private String surname;
    private String symptome;
    private String diagnostique;
    private String ordonance;
    private String commentaire;
    private LocalDate consultationDate;
    private Long doctorId;

    // getters / setters
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }
    public String getSymptome() { return symptome; }
    public void setSymptome(String symptome) { this.symptome = symptome; }
    public String getDiagnostique() { return diagnostique; }
    public void setDiagnostique(String diagnostique) { this.diagnostique = diagnostique; }
    public String getOrdonance() { return ordonance; }
    public void setOrdonance(String ordonance) { this.ordonance = ordonance; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public LocalDate getConsultationDate() { return consultationDate; }
    public void setConsultationDate(LocalDate consultationDate) { this.consultationDate = consultationDate; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
}
