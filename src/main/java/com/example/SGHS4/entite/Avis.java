package com.example.SGHS4.entite;

import jakarta.persistence.*;

@Entity
@Table(name = "avis")
public class Avis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String message;
    private String statut;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id", referencedColumnName = "id")  // Explicitly define the foreign key
    private Utilisateur utilisateur;

    public Avis() {
    }

    public Avis(String message, String statut, Utilisateur utilisateur) {
        this.message = message;
        this.statut = statut;
        this.utilisateur = utilisateur;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }
}
