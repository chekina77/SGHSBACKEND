package com.example.SGHS4.exceptions;


public class TokenExpireException extends RuntimeException {
    public TokenExpireException(String message) {
        super(message);
    }
}