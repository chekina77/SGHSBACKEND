package com.example.SGHS4.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ModificationMdpDTO {

        @NotBlank(message = "L'ancien mot de passe est obligatoire")
        private String ancienMotDePasse;

        @NotBlank(message = "Le nouveau mot de passe est obligatoire")
        @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
                message = "Le mot de passe doit contenir au moins une majuscule, une minuscule, un chiffre et un caractère spécial"
        )
        private String nouveauMotDePasse;

        @NotBlank(message = "La confirmation du nouveau mot de passe est obligatoire")
        private String confirmationNouveauMotDePasse;

        // Constructeur par défaut
        public ModificationMdpDTO() {
        }

        // Constructeur avec paramètres
        public ModificationMdpDTO(String ancienMotDePasse, String nouveauMotDePasse, String confirmationNouveauMotDePasse) {
                this.ancienMotDePasse = ancienMotDePasse;
                this.nouveauMotDePasse = nouveauMotDePasse;
                this.confirmationNouveauMotDePasse = confirmationNouveauMotDePasse;
        }

        // Getters et setters
        public String getAncienMotDePasse() {
                return ancienMotDePasse;
        }

        public void setAncienMotDePasse(String ancienMotDePasse) {
                this.ancienMotDePasse = ancienMotDePasse;
        }

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
}