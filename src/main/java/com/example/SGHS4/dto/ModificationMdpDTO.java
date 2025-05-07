package com.example.SGHS4.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ModificationMdpDTO {

        @NotBlank(message = "Le nouveau mot de passe est obligatoire")
        private String nouveauMotDePasse;

        @NotBlank(message = "La confirmation du mot de passe est obligatoire")
        private String confirmationNouveauMotDePasse;

        @NotBlank(message = "Le code de réinitialisation est obligatoire")
        private String codeReinitialisation;

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Email invalide")
        private String email;

        public String getNouveauMotDePasse() {
                return nouveauMotDePasse;
        }

        public void setNouveauMotDePasse(String nouveauMotDePasse) {
                this.nouveauMotDePasse = nouveauMotDePasse;
        }

        public String getConfirmationNouveauMotDePasse() {
                return confirmationNouveauMotDePasse;
        }

        public void setConfirmationNouveauMotDePasse(String confirmationNouveauMotDePasse) {
                this.confirmationNouveauMotDePasse = confirmationNouveauMotDePasse;
        }

        public String getCodeReinitialisation() {
                return codeReinitialisation;
        }

        public void setCodeReinitialisation(String codeReinitialisation) {
                this.codeReinitialisation = codeReinitialisation;
        }

        public String getEmail() {
                return email;
        }

        public void setEmail(String email) {
                this.email = email;
        }
        // Constructeurs, getters, setters
}
