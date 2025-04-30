package com.example.SGHS4.entite;

import com.example.SGHS4.enums.TypeValidation;
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

    /**
     * Indique si le code est actif (non utilisé et non expiré)
     */
    @Column(nullable = false)
    private boolean actif = true;

    /**
     * Type de validation (activation, réinitialisation de mot de passe, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeValidation type = TypeValidation.ACTIVATION;

    @OneToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    // Constructeur vide
    public Validation() {}

    // Constructeur avec paramètres
    public Validation(int id, Instant creation, Instant expiration, Instant activation, String code,
                      boolean actif, TypeValidation type, Utilisateur utilisateur) {
        this.id = id;
        this.creation = creation;
        this.expiration = expiration;
        this.activation = activation;
        this.code = code;
        this.actif = actif;
        this.type = type;
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

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public TypeValidation getType() {
        return type;
    }

    public void setType(TypeValidation type) {
        this.type = type;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    /**
     * Vérifie si le code est expiré
     * @return vrai si le code est expiré
     */
    public boolean isExpired() {
        return Instant.now().isAfter(this.expiration);
    }

}