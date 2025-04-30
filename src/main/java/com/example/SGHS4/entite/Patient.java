package com.example.SGHS4.entite;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String surname;
    private String sexe;
    private LocalDate dateofbirth;
    private double weight;
    private double height;
    private String email;
    private String nationalIDcardnumber;
    private String comment;
    private LocalDate dateoftoday;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public LocalDate getDateofbirth() {
        return dateofbirth;
    }

    public void setDateofbirth(LocalDate dateofbirth) {
        this.dateofbirth = dateofbirth;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNationalIDcardnumber() {
        return nationalIDcardnumber;
    }

    public void setNationalIDcardnumber(String nationalIDcardnumber) {
        this.nationalIDcardnumber = nationalIDcardnumber;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDate getDateoftoday() {
        return dateoftoday;
    }

    public void setDateoftoday(LocalDate dateoftoday) {
        this.dateoftoday = dateoftoday;
    }

    // Getters et Setters
    // ... (ajoutez tous les getters et setters pour les champ ci-dessus)
}