package com.example.SGHS4.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO pour la création d'un rendez-vous médical
 */
public class CreateAppointmentDTO {

    @NotNull(message = "L'ID du médecin est obligatoire")
    private Long doctorId;

    @NotBlank(message = "La date du rendez-vous est obligatoire")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}(T\\d{2}:\\d{2}:\\d{2})?$",
            message = "Format de date invalide. Utilisez le format ISO (yyyy-MM-dd ou yyyy-MM-ddTHH:mm:ss)")
    private String date;

    @Valid
    @NotNull(message = "Les informations du patient sont obligatoires")
    private PatientDTO patient;

    /**
     * DTO imbriqué pour les informations du patient
     */
    public static class PatientDTO {
        @NotBlank(message = "Le nom du patient est obligatoire")
        private String name;

        @NotBlank(message = "Le prénom du patient est obligatoire")
        private String surname;

        @Pattern(regexp = "^(M|F)$", message = "Le sexe doit être 'M' ou 'F'")
        private String sexe;

        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Format de date de naissance invalide. Utilisez le format yyyy-MM-dd")
        private String dateofbirth;

        private Double weight;

        private Double height;

        private String email;

        @NotBlank(message = "Le numéro de carte d'identité est obligatoire")
        private String nationalIDcardnumber;

        private String comment;

        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Format de date invalide. Utilisez le format yyyy-MM-dd")
        private String dateoftoday;

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

        public String getDateofbirth() {
            return dateofbirth;
        }

        public void setDateofbirth(String dateofbirth) {
            this.dateofbirth = dateofbirth;
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

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getDateoftoday() {
            return dateoftoday;
        }

        public void setDateoftoday(String dateoftoday) {
            this.dateoftoday = dateoftoday;
        }
    }

    // Getters et Setters
    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public PatientDTO getPatient() {
        return patient;
    }

    public void setPatient(PatientDTO patient) {
        this.patient = patient;
    }
}