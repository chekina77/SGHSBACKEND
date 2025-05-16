package com.example.SGHS4.dto;


import com.example.SGHS4.enums.TypeDeRole;

public class UtilisateurDTO {

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String cni;
    private boolean actif;
    private TypeDeRole role;

    // Getters et setters

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

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
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

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public TypeDeRole getRole() {
        return role;
    }

    public void setRole(TypeDeRole role) {
        this.role = role;
    }
}
