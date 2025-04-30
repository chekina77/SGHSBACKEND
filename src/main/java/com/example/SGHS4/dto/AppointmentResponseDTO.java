package com.example.SGHS4.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * DTO pour la réponse d'un rendez-vous médical
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppointmentResponseDTO {

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;

    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private DoctorDTO doctor;

    private PatientDTO patient;

    /**
     * DTO imbriqué pour les informations du médecin
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DoctorDTO {
        private Long id;
        private String name;
        private String specialization;

        public DoctorDTO() {
        }

        public DoctorDTO(Long id, String name, String specialization) {
            this.id = id;
            this.name = name;
            this.specialization = specialization;
        }

        // Getters et Setters
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

        public String getSpecialization() {
            return specialization;
        }

        public void setSpecialization(String specialization) {
            this.specialization = specialization;
        }
    }

    /**
     * DTO imbriqué pour les informations du patient
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PatientDTO {
        private Long id;
        private String name;
        private String surname;
        private String sexe;
        private String dateOfBirth;
        private Double weight;
        private Double height;
        private String email;
        private String nationalIdCardNumber;

        public PatientDTO() {
        }

        public PatientDTO(Long id, String name, String surname, String sexe, String dateOfBirth,
                          Double weight, Double height, String email, String nationalIdCardNumber) {
            this.id = id;
            this.name = name;
            this.surname = surname;
            this.sexe = sexe;
            this.dateOfBirth = dateOfBirth;
            this.weight = weight;
            this.height = height;
            this.email = email;
            this.nationalIdCardNumber = nationalIdCardNumber;
        }

        // Getters et Setters
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

        public String getDateOfBirth() {
            return dateOfBirth;
        }

        public void setDateOfBirth(String dateOfBirth) {
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

        public String getNationalIdCardNumber() {
            return nationalIdCardNumber;
        }

        public void setNationalIdCardNumber(String nationalIdCardNumber) {
            this.nationalIdCardNumber = nationalIdCardNumber;
        }
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public DoctorDTO getDoctor() {
        return doctor;
    }

    public void setDoctor(DoctorDTO doctor) {
        this.doctor = doctor;
    }

    public PatientDTO getPatient() {
        return patient;
    }

    public void setPatient(PatientDTO patient) {
        this.patient = patient;
    }
}