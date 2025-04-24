package com.example.SGHS4.entite;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "validation")
public class Validation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private Instant creation;

    @Column(nullable = false)
    private Instant expiration;

    private Instant activation;

    @Column(nullable = false, unique = true)
    private String code;

    @OneToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    // Constructeur vide
    public Validation() {}

    // Constructeur avec paramètres
    public Validation(int id, Instant creation, Instant expiration, Instant activation, String code, Utilisateur utilisateur) {
        this.id = id;
        this.creation = creation;
        this.expiration = expiration;
        this.activation = activation;
        this.code = code;
        this.utilisateur = utilisateur;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Instant getCreation() {
        return creation;
    }

    public void setCreation(Instant creation) {
        this.creation = creation;
    }

    public Instant getExpiration() {
        return expiration;
    }

    public void setExpiration(Instant expiration) {
        this.expiration = expiration;
    }

    public Instant getActivation() {
        return activation;
    }

    public void setActivation(Instant activation) {
        this.activation = activation;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }
}
