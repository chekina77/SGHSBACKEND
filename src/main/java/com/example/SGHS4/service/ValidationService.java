package com.example.SGHS4.service;

import com.example.SGHS4.entite.Utilisateur;
import com.example.SGHS4.entite.Validation;
import com.example.SGHS4.entite.PendingPersonnel;
import com.example.SGHS4.exceptions.ValidationException;
import com.example.SGHS4.repository.ValidationRepository;
import com.example.SGHS4.repository.PendingPersonnelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@Transactional
public class ValidationService {

    private static final Logger logger = LoggerFactory.getLogger(ValidationService.class);

    private final ValidationRepository validationRepository;
    private final NotificationService notificationService;
    private final PendingPersonnelRepository pendingPersonnelRepository;

    @Value("${validation.code.expiration-minutes:60}")
    private long codeExpirationMinutes;

    @Value("${validation.code.length:6}")
    private int codeLength;

    @Value("${validation.code.type:numeric}")
    private String codeType;

    @Autowired
    public ValidationService(ValidationRepository validationRepository,
                             NotificationService notificationService,
                             PendingPersonnelRepository pendingPersonnelRepository) {
        this.validationRepository = validationRepository;
        this.notificationService = notificationService;
        this.pendingPersonnelRepository = pendingPersonnelRepository;
    }

    public String enregistrer(Utilisateur utilisateur) {
        desactiverCodesExistants(utilisateur);

        String code = genererCode();

        Validation validation = new Validation();
        validation.setUtilisateur(utilisateur);
        validation.setCode(code);
        validation.setCreation(Instant.now());
        validation.setExpiration(Instant.now().plus(codeExpirationMinutes, ChronoUnit.MINUTES));
        validation.setActif(true);

        validationRepository.save(validation);

        try {
            notificationService.envoyer(validation);
            logger.info("Notification envoyée à l'utilisateur : {}", utilisateur.getEmail());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification : {}", e.getMessage());
        }

        return code;
    }

    public String enregistrerEnAttente(PendingPersonnel pendingPersonnel) {
        // Sauvegarde du personnel en attente avant de créer la validation
        pendingPersonnel = pendingPersonnelRepository.save(pendingPersonnel);

        desactiverCodesExistants(pendingPersonnel);

        String code = genererCode();

        Validation validation = new Validation();
        validation.setPendingPersonnel(pendingPersonnel);  // Utilisation du personnel en attente sauvegardé
        validation.setCode(code);
        validation.setCreation(Instant.now());
        validation.setExpiration(Instant.now().plus(codeExpirationMinutes, ChronoUnit.MINUTES));
        validation.setActif(true);

        validationRepository.save(validation);

        try {
            notificationService.envoyer(validation);
            logger.info("Notification envoyée au personnel en attente : {}", pendingPersonnel.getEmail());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification : {}", e.getMessage());
        }

        return code;
    }

    public Validation lireEnFonctionDuCode(String code) {
        logger.debug("Tentative d'activation avec le code : {}", code);

        Validation validation = validationRepository.findByCodeAndActif(code, true)
                .orElseThrow(() -> new ValidationException("Le code d'activation est invalide"));

        // Vérification de l'expiration du code
        logger.debug("Now: {}", Instant.now());
        logger.debug("Code expiration: {}", validation.getExpiration());
//        if (Instant.now().isAfter(validation.getExpiration())) {
//            validation.setActif(false);
//            validationRepository.save(validation);
//            throw new ValidationException("Le code d'activation a expiré. Veuillez demander un nouveau code.");
//        }

        return validation;
    }

    public boolean estValide(String code) {
        try {
            lireEnFonctionDuCode(code);
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }

    public void desactiver(String code) {
        validationRepository.findByCode(code).ifPresent(validation -> {
            validation.setActif(false);
            validationRepository.save(validation);
            logger.info("Code de validation désactivé : {}", code);
        });
    }

    private void desactiverCodesExistants(Utilisateur utilisateur) {
        // Désactiver les codes existants avant de créer un nouveau code
        List<Validation> validationsExistantes = validationRepository.findByUtilisateurAndActif(utilisateur, true);
        if (!validationsExistantes.isEmpty()) {
            validationsExistantes.forEach(validation -> validation.setActif(false));
            validationRepository.saveAll(validationsExistantes);
        }
    }

    private void desactiverCodesExistants(PendingPersonnel pendingPersonnel) {
        // Désactiver les codes existants pour un personnel en attente avant de créer un nouveau code
        List<Validation> validationsExistantes = validationRepository.findByPendingPersonnelAndActif(pendingPersonnel, true);
        if (!validationsExistantes.isEmpty()) {
            validationsExistantes.forEach(validation -> validation.setActif(false));
            validationRepository.saveAll(validationsExistantes);
        }
    }

    String genererCode() {
        if ("uuid".equals(codeType)) {
            return UUID.randomUUID().toString().substring(0, codeLength);
        } else if ("alphanumeric".equals(codeType)) {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            StringBuilder sb = new StringBuilder();
            Random random = new Random();
            for (int i = 0; i < codeLength; i++) {
                int index = random.nextInt(chars.length());
                sb.append(chars.charAt(index));
            }
            return sb.toString();
        } else {
            int randomInteger = new Random().nextInt((int) Math.pow(10, codeLength));
            return String.format("%0" + codeLength + "d", randomInteger);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void nettoyerCodesExpires() {
        logger.info("Début du nettoyage des codes de validation expirés");
        Instant now = Instant.now();
        List<Validation> validationsExpirees = validationRepository.findByExpirationBefore(now);

        if (!validationsExpirees.isEmpty()) {
            validationsExpirees.forEach(validation -> validation.setActif(false));
            validationRepository.saveAll(validationsExpirees);
            logger.info("{} codes de validation expirés ont été désactivés", validationsExpirees.size());
        } else {
            logger.info("Aucun code de validation expiré trouvé");
        }
    }

    public void supprimer(Validation validation) {
        if (validation.getPendingPersonnel() != null) {
            pendingPersonnelRepository.delete(validation.getPendingPersonnel());
            logger.info("Personnel en attente supprimé : {}", validation.getPendingPersonnel().getNom());
        }
        validationRepository.delete(validation);
        logger.info("Validation supprimée : {}", validation.getCode());
    }

    public void supprimer(Long validationId) {
        Validation validation = validationRepository.findById(validationId)
                .orElseThrow(() -> new ValidationException("Validation non trouvée"));
        supprimer(validation);
    }

    public PendingPersonnel getPendingPersonnel(String code) {
        Validation validation = validationRepository.findByCodeAndActif(code, true)
                .orElseThrow(() -> new ValidationException("Le code d'activation est invalide"));
        return validation.getPendingPersonnel();
    }
}
