package com.example.SGHS4.entite;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Livret {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne private Patient patient;

    private String diagnostique;
    private String ordonnance;
    private String commentaire;
    private LocalDate consultationDate;

    @ManyToOne
    @JsonIgnore  // Ne pas exposer les informations du médecin dans le JSON
    private Utilisateur doctor;

    @OneToMany(mappedBy = "livret", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference   // côté parent de la relation
    private Set<Consultation> consultations = new HashSet<>();

    public void addConsultation(Consultation c) {
        consultations.add(c);
        c.setLivret(this);
    }

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

    public String getDiagnostique() {
        return diagnostique;
    }

    public void setDiagnostique(String diagnostique) {
        this.diagnostique = diagnostique;
    }

    public String getOrdonnance() {
        return ordonnance;
    }

    public void setOrdonnance(String ordonnance) {
        this.ordonnance = ordonnance;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public LocalDate getConsultationDate() {
        return consultationDate;
    }

    public void setConsultationDate(LocalDate consultationDate) {
        this.consultationDate = consultationDate;
    }

    public Utilisateur getDoctor() {
        return doctor;
    }

    public void setDoctor(Utilisateur doctor) {
        this.doctor = doctor;
    }

    public Set<Consultation> getConsultations() {
        return consultations;
    }

    public void setConsultations(Set<Consultation> consultations) {
        this.consultations = consultations;
    }
    // getters & setters...
}
