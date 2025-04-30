package com.example.SGHS4.exceptions;

public class UtilisateurExisteDejaException extends RuntimeException {
    public UtilisateurExisteDejaException(String message) {
        super(message);
    }
}