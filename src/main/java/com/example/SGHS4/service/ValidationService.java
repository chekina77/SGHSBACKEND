package com.example.SGHS4.service;

import com.example.SGHS4.entite.Utilisateur;
import com.example.SGHS4.entite.Validation;
import com.example.SGHS4.exceptions.ValidationException;
import com.example.SGHS4.repository.ValidationRepository;
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

    @Value("${validation.code.expiration-minutes:10}")
    private long codeExpirationMinutes;

    @Value("${validation.code.length:6}")
    private int codeLength;

    @Value("${validation.code.type:numeric}")
    private String codeType;

    @Autowired
    public ValidationService(ValidationRepository validationRepository, NotificationService notificationService) {
        this.validationRepository = validationRepository;
        this.notificationService = notificationService;
    }

    /**
     * Enregistre un code de validation pour un utilisateur et envoie une notification
     * @param utilisateur L'utilisateur pour lequel générer un code de validation
     * @return Le code de validation généré
     */
    public String enregistrer(Utilisateur utilisateur) {
        // Désactiver les anciens codes de validation de l'utilisateur
        desactiverCodesExistants(utilisateur);

        // Générer un nouveau code
        String code = genererCode();

        Validation validation = new Validation();
        validation.setUtilisateur(utilisateur);
        validation.setCode(code);
        validation.setCreation(Instant.now());
        validation.setExpiration(Instant.now().plus(codeExpirationMinutes, ChronoUnit.MINUTES));
        validation.setActif(true);

        logger.info("Code de validation généré pour l'utilisateur : {}", utilisateur.getEmail());

        // Sauvegarder la validation
        validationRepository.save(validation);

        // Envoyer la notification
        try {
            notificationService.envoyer(validation);
            logger.info("Notification envoyée à l'utilisateur : {}", utilisateur.getEmail());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification : {}", e.getMessage());
        }

        return code;
    }

    /**
     * Renvoie un nouveau code de validation pour un utilisateur
     * @param email L'email de l'utilisateur
     * @return Le nouveau code de validation
     * @throws ValidationException Si l'utilisateur n'existe pas
     */
    public String regenererCode(String email) {
        Utilisateur utilisateur = validationRepository.findUtilisateurByEmail(email)
                .orElseThrow(() -> new ValidationException("Aucun utilisateur trouvé avec cet email"));

        return enregistrer(utilisateur);
    }

    /**
     * Lit une validation en fonction du code
     * @param code Le code de validation
     * @return La validation correspondante
     * @throws ValidationException Si le code est invalide ou expiré
     */
    public Validation lireEnFonctionDuCode(String code) {
        logger.debug("Tentative d'activation avec le code : {}", code);

        Validation validation = validationRepository.findByCodeAndActif(code, true)
                .orElseThrow(() -> new ValidationException("Le code d'activation est invalide"));

        // Vérifier si le code est expiré
        if (Instant.now().isAfter(validation.getExpiration())) {
            validation.setActif(false);
            validationRepository.save(validation);
            throw new ValidationException("Le code d'activation est expiré");
        }

        return validation;
    }

    /**
     * Vérifie si un code de validation est valide
     * @param code Le code à vérifier
     * @return true si le code est valide, false sinon
     */
    public boolean estValide(String code) {
        try {
            Validation validation = lireEnFonctionDuCode(code);
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }

    /**
     * Désactive un code de validation après utilisation
     * @param code Le code à désactiver
     */
    public void desactiver(String code) {
        validationRepository.findByCode(code).ifPresent(validation -> {
            validation.setActif(false);
            validationRepository.save(validation);
            logger.info("Code de validation désactivé : {}", code);
        });
    }

    /**
     * Désactive tous les codes existants pour un utilisateur
     * @param utilisateur L'utilisateur concerné
     */
    private void desactiverCodesExistants(Utilisateur utilisateur) {
        List<Validation> validationsExistantes = validationRepository.findByUtilisateurAndActif(utilisateur, true);
        validationsExistantes.forEach(validation -> validation.setActif(false));
        validationRepository.saveAll(validationsExistantes);
    }

    /**
     * Génère un code aléatoire selon la configuration
     * @return Le code généré
     */
    private String genererCode() {
        if ("uuid".equals(codeType)) {
            // Générer un UUID court
            return UUID.randomUUID().toString().substring(0, codeLength);
        } else if ("alphanumeric".equals(codeType)) {
            // Générer un code alphanumérique
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            StringBuilder sb = new StringBuilder();
            Random random = new Random();
            for (int i = 0; i < codeLength; i++) {
                int index = random.nextInt(chars.length());
                sb.append(chars.charAt(index));
            }
            return sb.toString();
        } else {
            // Par défaut, générer un code numérique
            int randomInteger = new Random().nextInt((int) Math.pow(10, codeLength));
            return String.format("%0" + codeLength + "d", randomInteger);
        }
    }

    /**
     * Nettoyage périodique des codes de validation expirés
     * Exécuté tous les jours à minuit
     */
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
}