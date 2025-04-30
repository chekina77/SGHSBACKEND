package com.example.SGHS4.dto;

import java.time.LocalDate;

public class PatientDTO {

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

    // Getters et Setters

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
}
