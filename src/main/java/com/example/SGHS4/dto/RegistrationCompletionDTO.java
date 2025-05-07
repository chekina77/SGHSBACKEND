package com.example.SGHS4.dto;

import jakarta.validation.constraints.NotBlank;

public class RegistrationCompletionDTO {

        @NotBlank(message = "Le CNI est obligatoire")
        private String cni;

        @NotBlank(message = "Le code de vérification est obligatoire")
        private String verificationCode;

        @NotBlank(message = "Le mot de passe ne peut pas être vide")
        private String password;

        // Getters et setters
        public String getCni() {
                return cni;
        }

        public void setCni(String cni) {
                this.cni = cni;
        }

        public String getVerificationCode() {
                return verificationCode;
        }

        public void setVerificationCode(String verificationCode) {
                this.verificationCode = verificationCode;
        }

        public String getPassword() {
                return password;
        }

        public void setPassword(String password) {
                this.password = password;
        }
}
