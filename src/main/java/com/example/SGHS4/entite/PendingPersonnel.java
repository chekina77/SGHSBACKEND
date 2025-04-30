package com.example.SGHS4.entite;

import com.example.SGHS4.enums.TypeDeRole;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "PendingPersonnel")
public class PendingPersonnel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cni;  // Ajout du champ CNI
    private String nom;
    private String telephone;
    private String email;

    @Enumerated(EnumType.STRING)
    private TypeDeRole role;

    private String verificationCode;
    private LocalDateTime verificationCodeExpiry;

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCni() {
        return cni;  // Retourne la CNI
    }

    public void setCni(String cni) {
        this.cni = cni;  // Assure que le champ CNI est correctement initialisé
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public TypeDeRole getRole() {
        return role;  // Retourne le rôle de type TypeDeRole
    }

    public void setRole(TypeDeRole role) {
        this.role = role;  // Assure-toi que le type correspond
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public LocalDateTime getVerificationCodeExpiry() {
        return verificationCodeExpiry;
    }

    public void setVerificationCodeExpiry(LocalDateTime verificationCodeExpiry) {
        this.verificationCodeExpiry = verificationCodeExpiry;
    }
}
