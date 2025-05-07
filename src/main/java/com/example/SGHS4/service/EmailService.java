package com.example.SGHS4.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // Lien vers le frontend (modifiable via application.properties si besoin)
    @Value("${frontend.verification.link:http://localhost:5173/CompleteRegistration?code=}")
    private String frontendLink;

    // Durée d'expiration du code en heures (par exemple 2 heures)
    private final int codeExpirationHours = 2;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Envoi du code d'activation par email avec lien complet
    public void envoyerCodeActivation(String toEmail, String codeActivation) {
        String fullLink = frontendLink + codeActivation;

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Code d'activation - Système de Gestion Hospitalière");
            message.setText(
                    "Bonjour,\n\n" +
                            "Votre code de vérification est : " + codeActivation + "\n" +
                            "Ce code expire dans " + codeExpirationHours + " heures.\n\n" +
                            "Pour finaliser votre inscription, veuillez cliquer sur ce lien :\n" +
                            fullLink + "\n\n" +
                            "Cordialement,\nL'équipe d'administration"
            );

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Ajoute ceci dans EmailService.java
    public void envoyerCodeReinitialisation(String toEmail, String codeReinitialisation) {
        String fullLink = frontendLink + codeReinitialisation;

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Réinitialisation du mot de passe - Système de Gestion Hospitalière");
            message.setText(
                    "Bonjour,\n\n" +
                            "Vous avez demandé une réinitialisation de mot de passe.\n" +
                            "Votre code de réinitialisation est : " + codeReinitialisation + "\n" +
                            "Ce code expirera dans 24 heures.\n\n" +  // Mise à jour de l'expiration
                            "Si vous n'êtes pas à l'origine de cette demande, veuillez ignorer ce message.\n\n" +
                            "Cordialement,\nL'équipe d'administration"
            );

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'e-mail de réinitialisation : " + e.getMessage());
            e.printStackTrace();
        }
    }
}