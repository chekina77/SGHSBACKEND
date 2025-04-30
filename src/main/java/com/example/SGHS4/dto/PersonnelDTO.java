package com.example.SGHS4.dto;

import com.example.SGHS4.enums.TypeDeRole;

public class PersonnelDTO {
    private String cni;  // CNI placé au-dessus de nom
    private String nom;
    private String telephone;
    private String email;
    private String role;

    // Getter et Setter pour le numéro de CNI
    public String getCni() {
        return cni;
    }

    public void setCni(String cni) {
        this.cni = cni;
    }

    // Getters et Setters pour le nom, téléphone, email
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

    // Getter et Setter pour le rôle
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Méthode pour convertir le rôle en TypeDeRole (Enum)
    public TypeDeRole getRoleEnum() {
        if (this.role == null || this.role.isBlank()) {
            return null; // ou une valeur par défaut, ou on pourrait lancer une exception ici
        }

        try {
            // On s'assure que le rôle est valide en le convertissant à une valeur de l'énumération TypeDeRole
            return TypeDeRole.valueOf(this.role.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            // Loggez l'erreur ou gérez-la autrement
            return null; // Ou lancer une exception spécifique pour indiquer un rôle invalide
        }
    }
}
