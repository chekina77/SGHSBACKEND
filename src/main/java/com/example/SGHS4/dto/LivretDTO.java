package com.example.SGHS4.dto;

import com.example.SGHS4.entite.Livret;
import com.example.SGHS4.util.AESUtil;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class LivretDTO {
    private Long id;
    private Long patientId;
    private String patientName; // ✅ Nom du patient déchiffré
    private Long doctorId;

    private String diagnostique;
    private String ordonnance;
    private String commentaire;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate consultationDate;

    private int viewCount;  // nombre de consultations

    public LivretDTO() {
    }

    // Constructeur depuis l'entité (non chiffré)
    public LivretDTO(Livret livret) {
        this.id = livret.getId();
        this.patientId = livret.getPatient().getId();
        this.patientName = livret.getPatient().getName();
        this.doctorId = livret.getDoctor().getId();
        this.consultationDate = livret.getConsultationDate();
        this.diagnostique = livret.getDiagnostique();
        this.ordonnance = livret.getOrdonnance();
        this.commentaire = livret.getCommentaire();
        this.viewCount = livret.getViewCount();
    }

    // Constructeur avec déchiffrement AES, y compris patientName
    public LivretDTO(Livret livret, String secretKey) {
        this.id = livret.getId();
        this.patientId = livret.getPatient().getId();
        try {
            this.patientName = AESUtil.decrypt(livret.getPatient().getName(), secretKey);
        } catch (Exception e) {
            this.patientName = "Erreur déchiffrement nom patient";
        }
        this.doctorId = livret.getDoctor().getId();
        this.consultationDate = livret.getConsultationDate();

        try {
            this.diagnostique = AESUtil.decrypt(livret.getDiagnostique(), secretKey);
            this.ordonnance = AESUtil.decrypt(livret.getOrdonnance(), secretKey);
            this.commentaire = AESUtil.decrypt(livret.getCommentaire(), secretKey);
        } catch (Exception e) {
            this.diagnostique = "Erreur déchiffrement";
            this.ordonnance = "Erreur déchiffrement";
            this.commentaire = "Erreur déchiffrement";
        }
        this.viewCount = livret.getViewCount();
    }

    // === Getters & Setters ===

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
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

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }
}
