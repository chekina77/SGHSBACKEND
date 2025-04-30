package com.example.SGHS4.dto;

import java.time.LocalDateTime;

public class CreateAppointmentDTO {
    private Long id;
    private PatientDTO patient;
    private LocalDateTime date;
    private Long doctorId;

    public PatientDTO getPatient() {
        return patient;
    }

    public void setPatient(PatientDTO patient) {
        this.patient = patient;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    // Getters et Setters
    // ... (ajoutez tous les getters et setters)

    public static class PatientDTO {
        private String name;
        private String surname;
        private String sexe;
        private String dateofbirth;
        private double weight;
        private double height;
        private String email;
        private String nationalIDcardnumber;
        private String comment;
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

        public String getDateoftoday() {
            return dateoftoday;
        }

        public void setDateoftoday(String dateoftoday) {
            this.dateoftoday = dateoftoday;
        }
        // ... (ajoutez tous les getters et setters)
    }
}