package com.example.SGHS4.service;

import com.example.SGHS4.entite.Validation;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final JavaMailSender javaMailSender;

    public NotificationService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void envoyer(Validation validation) {
        SimpleMailMessage message = new SimpleMailMessage();

        // Déterminer le destinataire (Utilisateur ou PendingPersonnel)
        String email;
        String nom;
        if (validation.getUtilisateur() != null) {
            email = validation.getUtilisateur().getEmail();
            nom = validation.getUtilisateur().getNom();
        } else if (validation.getPendingPersonnel() != null) {
            email = validation.getPendingPersonnel().getEmail();
            nom = validation.getPendingPersonnel().getNom();
        } else {
            throw new IllegalStateException("Aucun destinataire pour la notification");
        }

        message.setTo(email);
        message.setSubject("Votre code d'activation");

        String texte = String.format(
                "Bonjour %s,\n\nVotre code d'activation est : %s.\n\nÀ bientôt.",
                nom,
                validation.getCode()
        );

        message.setText(texte);
        javaMailSender.send(message);
    }
}