package com.example.SGHS4.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

/**
 * DTO pour la création d'un rendez-vous médical
 */
public class CreateAppointmentDTO {
    public String name;
    public String surname;
    public String sexe;
    public LocalDate dateOfBirth;
    public Double weight;
    public Double height;
    public String email;
    public String nationalIDcardnumber;
    public String phoneNumber;
    public String allergies;
    public String comment;
    public LocalDate dateOfToday;
    public Long doctorId;           // ← permet de choisir le médecin

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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDate getDateOfToday() {
        return dateOfToday;
    }

    public void setDateOfToday(LocalDate dateOfToday) {
        this.dateOfToday = dateOfToday;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }
}
