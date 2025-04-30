package com.example.SGHS4.dto;


import java.util.List;

public class JwtResponseDTO {
    private String token;
    private String refreshToken;
    private String email;
    private String nom;
    private List<String> roles;

    // Constructeur
    public JwtResponseDTO(String token, String refreshToken, String email, String nom, List<String> roles) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.email = email;
        this.nom = nom;
        this.roles = roles;
    }

    // Getters et Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}