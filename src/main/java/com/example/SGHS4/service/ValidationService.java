package com.example.SGHS4.service;

import com.example.SGHS4.entite.Utilisateur;
import com.example.SGHS4.entite.Validation;
import com.example.SGHS4.repository.ValidationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Service
public class ValidationService {

    private final ValidationRepository validationRepository;
    private final NotificationService notificationService;

    @Autowired
    public ValidationService(ValidationRepository validationRepository, NotificationService notificationService) {
        this.validationRepository = validationRepository;
        this.notificationService = notificationService;
    }

    public void enregistrer(Utilisateur utilisateur) {
        String code = genererCode();

        Validation validation = new Validation();
        validation.setUtilisateur(utilisateur);
        validation.setCode(code);
        validation.setCreation(Instant.now());
        validation.setExpiration(Instant.now().plus(10, ChronoUnit.MINUTES));

        System.out.println("Code généré : " + code);
        System.out.println("👤 Enregistré pour l'utilisateur : " + utilisateur.getEmail());

        validationRepository.save(validation);
        notificationService.envoyer(validation);
    }

    public Validation lireEnFonctionDuCode(String code) {
        System.out.println("Tentative d'activation avec le code : " + code);

        return validationRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException(" Le code d'activation est invalide ou expiré."));
    }

    private String genererCode() {
        int randomInteger = new Random().nextInt(1_000_000);
        return String.format("%06d", randomInteger);
    }
}
