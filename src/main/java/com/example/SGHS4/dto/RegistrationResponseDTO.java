package com.example.SGHS4.dto;


/**
 * DTO pour structurer la réponse d'enregistrement d'un personnel
 */
public class RegistrationResponseDTO {
    private boolean success;
    private String message;

    // Constructeurs
    public RegistrationResponseDTO() {
    }

    public RegistrationResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Getters et setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}