package com.example.SGHS4.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatientDTO {
    private Long id;
    private String name;
    private String surname;
    private String sexe;
    private LocalDate dateOfBirth;
    private Double weight;
    private Double height;
    private String email;
    private String nationalIDCardNumber;
    private String phoneNumber;
    private String allergies;
    private String comment;
    private LocalDate dateOfToday;
    private String medecinName;
    private String fingerprintHash; // Encodée en base64 ou hexadécimal



    // Constructeur vide pour Jackson
    public PatientDTO() {}

    /**
     * Constructeur partiel utilisé pour lister dans le tableau.
     * Seuls id, name, surname, email, phoneNumber et medecinName sont initialisés.
     */
    public PatientDTO(Long id,
                      String name,
                      String surname,
                      String email,
                      String phoneNumber,
                      String medecinName) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.medecinName = medecinName;
    }

    /** Constructeur complet si tu veux tout initialiser d’un coup */
    public PatientDTO(Long id,
                      String name,
                      String surname,
                      String sexe,
                      LocalDate dateOfBirth,
                      Double weight,
                      Double height,
                      String email,
                      String nationalIDCardNumber,
                      String phoneNumber,
                      String allergies,
                      String comment,
                      LocalDate dateOfToday,
                      String medecinName) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.sexe = sexe;
        this.dateOfBirth = dateOfBirth;
        this.weight = weight;
        this.height = height;
        this.email = email;
        this.nationalIDCardNumber = nationalIDCardNumber;
        this.phoneNumber = phoneNumber;
        this.allergies = allergies;
        this.comment = comment;
        this.dateOfToday = dateOfToday;
        this.medecinName = medecinName;
    }

    // ─── Getters & Setters ──────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getSexe() { return sexe; }
    public void setSexe(String sexe) { this.sexe = sexe; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNationalIDCardNumber() { return nationalIDCardNumber; }
    public void setNationalIDCardNumber(String nationalIDCardNumber) {
        this.nationalIDCardNumber = nationalIDCardNumber;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDate getDateOfToday() { return dateOfToday; }
    public void setDateOfToday(LocalDate dateOfToday) { this.dateOfToday = dateOfToday; }

    public String getMedecinName() { return medecinName; }
    public void setMedecinName(String medecinName) { this.medecinName = medecinName; }

    public String getFingerprintHash() {
        return fingerprintHash;
    }

    public void setFingerprintHash(String fingerprintHash) {
        this.fingerprintHash = fingerprintHash;
    }
}
