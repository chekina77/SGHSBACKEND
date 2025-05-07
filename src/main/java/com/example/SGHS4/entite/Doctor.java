package com.example.SGHS4.entite;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Représente un médecin dans la base de données.
 */
@Entity
@Table(name = "doctors")
public class Doctor {

    /** Identifiant unique généré automatiquement */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nom du médecin, obligatoire, longueur max 100 */
    @Column(nullable = false, length = 100)
    private String nom;

    /** Prénom du médecin, obligatoire, longueur max 100 */
    @Column(nullable = false, length = 100)
    private String prenom;

    /** Email unique du médecin, obligatoire, longueur max 150 */
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /** Numéro de téléphone du médecin, longueur max 20 */
    @Column(name = "phone_number", length = 20)
    private String telephone;

    /** Horodatage de création (automatique) */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Horodatage de dernière mise à jour (automatique) */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Doctor() {
        // constructeur pour JPA
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters et setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}