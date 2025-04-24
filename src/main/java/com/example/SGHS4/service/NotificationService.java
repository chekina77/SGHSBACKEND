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

        // message.setFrom("chekinae22@gmail.com");

        message.setTo(validation.getUtilisateur().getEmail());
        message.setSubject("Votre code d'activation");

        String texte = String.format(
                "Bonjour %s,\n\nVotre code d'activation est : %s.\n\nÀ bientôt.",
                validation.getUtilisateur().getNom(),
                validation.getCode()
        );

        message.setText(texte);
        javaMailSender.send(message);
    }
}
