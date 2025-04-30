package com.example.SGHS4.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegistrationCompletionDTO(
        @NotBlank(message = "Le numéro CNI est obligatoire")
        String cni,

        @NotBlank(message = "Le code de vérification est obligatoire")
        String verificationCode,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
                message = "Le mot de passe doit contenir au moins 8 caractères dont une majuscule, une minuscule, un chiffre et un caractère spécial"
        )
        String password
) {}