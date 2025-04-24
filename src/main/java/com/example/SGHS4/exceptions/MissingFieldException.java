package com.example.SGHS4.exceptions;


    public class MissingFieldException extends RuntimeException {
        public MissingFieldException(String message) {
            super(message);
        }
    }