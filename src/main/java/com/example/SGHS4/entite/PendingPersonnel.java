package com.example.SGHS4.entite;

import com.example.SGHS4.enums.TypeDeRole;
import com.example.SGHS4.entite.Validation;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "pending_personnel")
public class PendingPersonnel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String telephone;

    @Column(unique = true)
    private String cni;

    @Enumerated(EnumType.STRING)
    private TypeDeRole role;

    /**
     * Liste des validations associées à ce personnel en attente
     */
    @OneToMany(mappedBy = "pendingPersonnel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Validation> validations;

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getCni() {
        return cni;
    }

    public void setCni(String cni) {
        this.cni = cni;
    }

    public TypeDeRole getRole() {
        return role;
    }

    public void setRole(TypeDeRole role) {
        this.role = role;
    }

    public List<Validation> getValidations() {
        return validations;
    }

    public void setValidations(List<Validation> validations) {
        this.validations = validations;
    }
}
