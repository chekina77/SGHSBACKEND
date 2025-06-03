package com.example.SGHS4.entite;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class LabResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Résultat associé à un patient
    @ManyToOne
    private Patient patient;

    // Le laborantin qui a renseigné les infos
    @ManyToOne
    private Utilisateur laborantin;

    // Le médecin à qui les résultats sont destinés
    @ManyToOne
    private Utilisateur medecin;

    // Données sensibles (chiffrées avec CNI)
    private String testName;
    private String result;
    private String comment;

    private LocalDateTime createdAt = LocalDateTime.now();

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

    public Utilisateur getLaborantin() {
        return laborantin;
    }

    public void setLaborantin(Utilisateur laborantin) {
        this.laborantin = laborantin;
    }

    public Utilisateur getMedecin() {
        return medecin;
    }

    public void setMedecin(Utilisateur medecin) {
        this.medecin = medecin;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
