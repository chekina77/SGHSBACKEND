package com.example.SGHS4.dto;

import com.example.SGHS4.enums.TypeDeRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class PendingPersonnelDTO {
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Pattern(regexp = "^\\d{10}$", message = "Le numéro de téléphone doit contenir 10 chiffres")
    private String telephone;

    @NotBlank(message = "Le numéro de CNI est obligatoire")
    private String cni;

    @NotNull(message = "Le rôle est obligatoire")
    private TypeDeRole roleEnum;

    // Getters et Setters
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

    public TypeDeRole getRoleEnum() {
        return roleEnum;
    }

    public void setRoleEnum(TypeDeRole roleEnum) {
        this.roleEnum = roleEnum;
    }
}