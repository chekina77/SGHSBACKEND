package com.example.SGHS4.entite;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 512) // suffisant pour stocker la valeur chiffrée base64
    private String name;

    @Column(length = 512)
    private String surname;

    @Column(length = 512)
    private String sexe;

    private LocalDate dateOfBirth;

    private Double weight;
    private Double height;

    @Column(length = 512)
    private String email;

    @Column(length = 512, unique = true)
    private String encryptedCNI;  // ici CNI chiffrée (ex nationalIDcardnumber renommé)

    @Column(length = 512)
    private String phoneNumber;

    @Column(length = 1024)
    private String allergies;

    @Column(length = 2048)
    private String comment;

    private LocalDate dateOfToday;

    private String medecinName;

    private LocalDate lastVisit;

    @OneToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    // **Ajout de la clé patient chiffrée avec masterKey**
    @Column(length = 512)
    private String encryptedPatientKey;

    // **Constructeur sans argument pour JPA**
    public Patient() {}

    // **Constructeur avec ID pour simplification dans certaines situations**
    public Patient(Long id) {
        this.id = id;
    }

    // Getters & Setters

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

    public String getEncryptedCNI() { return encryptedCNI; }
    public void setEncryptedCNI(String encryptedCNI) { this.encryptedCNI = encryptedCNI; }

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

    public LocalDate getLastVisit() { return lastVisit; }
    public void setLastVisit(LocalDate lastVisit) { this.lastVisit = lastVisit; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }

    public String getEncryptedPatientKey() { return encryptedPatientKey; }
    public void setEncryptedPatientKey(String encryptedPatientKey) { this.encryptedPatientKey = encryptedPatientKey; }
}
