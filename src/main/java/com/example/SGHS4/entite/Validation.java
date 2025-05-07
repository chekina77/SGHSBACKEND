package com.example.SGHS4.entite;

import com.example.SGHS4.enums.TypeValidation;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "validation")
public class Validation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant creation;

    @PrePersist
    public void prePersist() {
        if (this.creation == null) {
            this.creation = Instant.now();
        }
    }

    @Column(nullable = false)
    private Instant expiration;

    private Instant activation;

    @Column(nullable = false) // ❌ supprimé unique = true
    private String code;

    @Column(nullable = false)
    private boolean actif = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeValidation type = TypeValidation.ACTIVATION;

    @OneToOne(optional = true)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    // ❗️On précise qu’il peut y avoir plusieurs validations pour un même pending personnel
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "pending_personnel_id", nullable = true)
    private PendingPersonnel pendingPersonnel;

    // Constructeur vide
    public Validation() {}

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public PendingPersonnel getPendingPersonnel() {
        return pendingPersonnel;
    }

    public void setPendingPersonnel(PendingPersonnel pendingPersonnel) {
        this.pendingPersonnel = pendingPersonnel;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiration);
    }
}
