package com.example.SGHS4.service;

import com.example.SGHS4.dto.AuthentificationDTO;
import com.example.SGHS4.dto.PersonnelDTO;
import com.example.SGHS4.enums.TypeDeRole;
import com.example.SGHS4.entite.PendingPersonnel;
import com.example.SGHS4.entite.Utilisateur;
import com.example.SGHS4.entite.Role;
import com.example.SGHS4.exceptions.UtilisateurExisteDejaException;
import com.example.SGHS4.exceptions.UtilisateurInactifException;
import com.example.SGHS4.exceptions.ValidationException;
import com.example.SGHS4.repository.PendingPersonnelRepository;
import com.example.SGHS4.repository.UtilisateurRepository;
import com.example.SGHS4.repository.RoleRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    // Expressions régulières pour la validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern TELEPHONE_PATTERN = Pattern.compile("^[0-9]{10,}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");

    private final PendingPersonnelRepository pendingPersonnelRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final JavaMailSender mailSender;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${validation.code.expiration-hours:24}")
    private int codeExpirationHours;

    @Value("${validation.code.length:6}")
    private int codeLength;

    @Autowired
    public AdminService(PendingPersonnelRepository pendingPersonnelRepository,
                        UtilisateurRepository utilisateurRepository,
                        RoleRepository roleRepository,
                        JavaMailSender mailSender,
                        BCryptPasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.pendingPersonnelRepository = pendingPersonnelRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Enregistre un nouveau membre du personnel
     * @param dto Les données du personnel à enregistrer
     * @return Un message de succès
     * @throws ValidationException Si les données sont invalides ou déjà utilisées
     */
    @Transactional
    public String registerPersonnel(PersonnelDTO dto) {
        logger.info("Tentative d'enregistrement d'un nouveau personnel avec email: {}", dto.getEmail());

        // Validation des données
        validatePersonnelData(dto);

        // Vérification des doublons
        checkDuplicateData(dto);

        // Création du PendingPersonnel
        PendingPersonnel pending = new PendingPersonnel();
        pending.setCni(dto.getCni());
        pending.setNom(dto.getNom());
        pending.setTelephone(dto.getTelephone());
        pending.setEmail(dto.getEmail());
        pending.setRole(dto.getRoleEnum());

        // Génération du code de vérification
        String code = generateVerificationCode();
        pending.setVerificationCode(code);
        pending.setVerificationCodeExpiry(LocalDateTime.now().plusHours(codeExpirationHours));

        // Sauvegarde et envoi du code
        pendingPersonnelRepository.save(pending);

        try {
            sendVerificationEmail(dto.getEmail(), code);
            logger.info("Personnel enregistré et code de vérification envoyé à: {}", dto.getEmail());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email de vérification: {}", e.getMessage());
            throw new ValidationException("Personnel enregistré mais erreur lors de l'envoi du code. Contactez l'administrateur.");
        }

        return "Utilisateur enregistré avec succès. Un email avec un code de vérification a été envoyé.";
    }

    /**
     * Vérifie et complète l'inscription d'un membre du personnel
     * @param cni Numéro CNI du personnel
     * @param code Code de vérification
     * @param password Mot de passe choisi
     * @return Message de résultat
     * @throws ValidationException Si les données sont invalides
     */
    @Transactional
    public String verifyAndCompleteRegistration(String cni, String code, String password) {
        logger.info("Tentative de finalisation d'inscription pour CNI: {}", cni);

        // Récupérer le personnel en attente
        PendingPersonnel pending = pendingPersonnelRepository.findByCni(cni)
                .orElseThrow(() -> {
                    logger.warn("Personnel avec CNI {} non trouvé", cni);
                    return new ValidationException("Personnel non trouvé.");
                });

        // Vérifier le code
        if (!pending.getVerificationCode().equals(code)) {
            logger.warn("Code de vérification incorrect pour CNI: {}", cni);
            throw new ValidationException("Code de vérification incorrect.");
        }

        // Vérifier si le code n'est pas expiré
        if (pending.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
            logger.warn("Code de vérification expiré pour CNI: {}", cni);
            throw new ValidationException("Le code de vérification a expiré. Veuillez contacter l'administrateur.");
        }

        // Valider le mot de passe
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            logger.warn("Mot de passe non conforme lors de la finalisation de l'inscription");
            throw new ValidationException("Le mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule, un chiffre et un caractère spécial.");
        }

        // Vérifier l'unicité des données
        if (utilisateurRepository.existsByTelephone(pending.getTelephone())) {
            logger.warn("Téléphone déjà utilisé: {}", pending.getTelephone());
            throw new UtilisateurExisteDejaException("Ce numéro de téléphone est déjà utilisé.");
        }

        if (utilisateurRepository.existsByEmail(pending.getEmail())) {
            logger.warn("Email déjà utilisé: {}", pending.getEmail());
            throw new UtilisateurExisteDejaException("Cet email est déjà utilisé.");
        }

        // Création de l'utilisateur
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setCni(pending.getCni());
        utilisateur.setNom(pending.getNom());
        utilisateur.setTelephone(pending.getTelephone());
        utilisateur.setEmail(pending.getEmail());
        utilisateur.setRole(pending.getRole());
        utilisateur.setMdp(passwordEncoder.encode(password));
        utilisateur.setActif(true);
        utilisateur.setMotDePasseTemporaire(false);
        utilisateur.setDateCreationMotDePasse(Instant.now());

        // Sauvegarde et suppression dans la même transaction
        utilisateurRepository.save(utilisateur);
        pendingPersonnelRepository.delete(pending);

        logger.info("Inscription finalisée avec succès pour: {}", utilisateur.getEmail());
        return "Inscription réussie et utilisateur créé.";
    }

    /**
     * Authentifie un administrateur
     * @param dto Données d'authentification
     * @return Map contenant les tokens d'accès
     * @throws BadCredentialsException Si les identifiants sont incorrects
     */
    @Transactional
    public Map<String, String> connexion(AuthentificationDTO dto) {
        logger.info("Tentative de connexion admin pour: {}", dto.email());

        // Rechercher l'utilisateur par email
        Utilisateur utilisateur = utilisateurRepository.findByEmail(dto.email())
                .orElseThrow(() -> {
                    logger.warn("Tentative de connexion avec un email inconnu: {}", dto.email());
                    return new UsernameNotFoundException("Aucun utilisateur ne correspond à cet e-mail.");
                });

        // Vérifier si le compte est actif
        if (!utilisateur.isActif()) {
            logger.warn("Tentative de connexion avec un compte inactif: {}", dto.email());
            throw new UtilisateurInactifException("Votre compte n'est pas activé. Veuillez vérifier votre email pour le code d'activation.");
        }

        // Vérifier si l'utilisateur a le rôle ADMIN
        if (!utilisateur.getRole().equals(TypeDeRole.ADMINISTRATEUR)) {
            logger.warn("Tentative de connexion admin par un non-admin: {}", dto.email());
            throw new BadCredentialsException("Vous n'avez pas les droits d'administrateur");
        }

        // Vérifier le mot de passe
        if (!passwordEncoder.matches(dto.password(), utilisateur.getMdp())) {
            logger.warn("Mot de passe incorrect pour: {}", dto.email());
            throw new BadCredentialsException("Identifiants incorrects");
        }

        // Générer les tokens JWT
        Map<String, String> tokens = jwtService.generate(utilisateur.getEmail());
        logger.info("Connexion admin réussie pour: {}", dto.email());

        return tokens;
    }

    /**
     * Récupère un personnel en attente d'activation par son CNI
     * @param cni Numéro CNI
     * @return Personnel en attente
     * @throws ValidationException Si le personnel n'est pas trouvé
     */
    @Transactional(readOnly = true)
    public PendingPersonnel findPendingByCni(String cni) {
        logger.info("Recherche de personnel en attente avec CNI: {}", cni);
        return pendingPersonnelRepository.findByCni(cni)
                .orElseThrow(() -> {
                    logger.warn("Aucun personnel en attente trouvé avec CNI: {}", cni);
                    return new ValidationException("Aucun enregistrement en attente pour ce numéro CNI.");
                });
    }

    /**
     * Liste tous les utilisateurs (membres du personnel)
     * @return Liste des utilisateurs
     */
    @Transactional(readOnly = true)
    public List<Utilisateur> getAllPersonnel() {
        logger.info("Récupération de la liste de tout le personnel");
        return utilisateurRepository.findAll();
    }

    /**
     * Réinitialise le processus d'inscription pour un personnel en attente
     * @param cni Numéro CNI
     * @return Nouveau code de vérification
     * @throws ValidationException Si le personnel n'est pas trouvé
     */
    @Transactional
    public String resetVerificationCode(String cni) {
        logger.info("Demande de réinitialisation du code pour CNI: {}", cni);

        PendingPersonnel pending = pendingPersonnelRepository.findByCni(cni)
                .orElseThrow(() -> {
                    logger.warn("Pas de personnel en attente trouvé avec CNI: {}", cni);
                    return new ValidationException("Aucun personnel en attente avec ce numéro CNI.");
                });

        String newCode = generateVerificationCode();
        pending.setVerificationCode(newCode);
        pending.setVerificationCodeExpiry(LocalDateTime.now().plusHours(codeExpirationHours));

        pendingPersonnelRepository.save(pending);

        try {
            sendVerificationEmail(pending.getEmail(), newCode);
            logger.info("Nouveau code de vérification envoyé à: {}", pending.getEmail());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi du nouveau code: {}", e.getMessage());
            throw new ValidationException("Erreur lors de l'envoi du nouveau code par email.");
        }

        return "Un nouveau code de vérification a été envoyé à " + pending.getEmail();
    }

    // Méthodes privées

    /**
     * Valide les données du personnel
     * @param dto Données du personnel
     * @throws ValidationException Si les données sont invalides
     */
    private void validatePersonnelData(PersonnelDTO dto) {
        if (dto == null) {
            logger.warn("Tentative d'enregistrement avec un DTO null");
            throw new ValidationException("Les données du personnel ne peuvent pas être nulles.");
        }

        if (dto.getNom() == null || dto.getNom().trim().isEmpty()) {
            logger.warn("Nom manquant ou vide");
            throw new ValidationException("Le nom est obligatoire.");
        }

        if (dto.getTelephone() == null || !TELEPHONE_PATTERN.matcher(dto.getTelephone()).matches()) {
            logger.warn("Numéro de téléphone invalide: {}", dto.getTelephone());
            throw new ValidationException("Le numéro de téléphone doit contenir au moins 10 chiffres.");
        }

        if (dto.getEmail() == null || !EMAIL_PATTERN.matcher(dto.getEmail()).matches()) {
            logger.warn("Adresse email invalide: {}", dto.getEmail());
            throw new ValidationException("L'adresse email est invalide.");
        }

        if (dto.getCni() == null || dto.getCni().trim().isEmpty()) {
            logger.warn("CNI manquant ou vide");
            throw new ValidationException("Le numéro CNI est obligatoire.");
        }

        if (dto.getRoleEnum() == null) {
            logger.warn("Rôle manquant");
            throw new ValidationException("Le rôle est obligatoire.");
        }

        // Vérifier si le rôle existe
        roleRepository.findByLibelle(dto.getRoleEnum())
                .orElseThrow(() -> {
                    logger.warn("Rôle non trouvé: {}", dto.getRoleEnum());
                    return new ValidationException("Rôle '" + dto.getRoleEnum() + "' non trouvé dans la base de données.");
                });
    }

    /**
     * Vérifie les doublons dans les données
     * @param dto Données du personnel
     * @throws UtilisateurExisteDejaException Si des données sont déjà utilisées
     */
    private void checkDuplicateData(PersonnelDTO dto) {
        if (isTelephoneAlreadyUsed(dto.getTelephone())) {
            logger.warn("Téléphone déjà utilisé: {}", dto.getTelephone());
            throw new UtilisateurExisteDejaException("Ce numéro de téléphone est déjà utilisé.");
        }

        if (isEmailAlreadyUsed(dto.getEmail())) {
            logger.warn("Email déjà utilisé: {}", dto.getEmail());
            throw new UtilisateurExisteDejaException("Cet email est déjà utilisé.");
        }

        if (isCniAlreadyUsed(dto.getCni())) {
            logger.warn("CNI déjà utilisé: {}", dto.getCni());
            throw new UtilisateurExisteDejaException("Un personnel avec ce numéro de CNI existe déjà.");
        }
    }

    /**
     * Vérifie si un numéro de téléphone est déjà utilisé
     * @param telephone Numéro de téléphone
     * @return true si déjà utilisé
     */
    private boolean isTelephoneAlreadyUsed(String telephone) {
        return pendingPersonnelRepository.existsByTelephone(telephone)
                || utilisateurRepository.existsByTelephone(telephone);
    }

    /**
     * Vérifie si un email est déjà utilisé
     * @param email Adresse email
     * @return true si déjà utilisé
     */
    private boolean isEmailAlreadyUsed(String email) {
        return pendingPersonnelRepository.existsByEmail(email)
                || utilisateurRepository.existsByEmail(email);
    }

    /**
     * Vérifie si un numéro CNI est déjà utilisé
     * @param cni Numéro CNI
     * @return true si déjà utilisé
     */
    private boolean isCniAlreadyUsed(String cni) {
        return cni != null && !cni.isEmpty()
                && (pendingPersonnelRepository.findByCni(cni).isPresent()
                || utilisateurRepository.existsByCni(cni));
    }

    /**
     * Génère un code de vérification aléatoire
     * @return Code de vérification
     */
    private String generateVerificationCode() {
        int max = (int) Math.pow(10, codeLength);
        return String.format("%0" + codeLength + "d", new Random().nextInt(max));
    }

    /**
     * Envoie un email avec le code de vérification
     * @param to Adresse email du destinataire
     * @param code Code de vérification
     */
    private void sendVerificationEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Code de vérification - Système de Gestion Hospitalière");
        message.setText("Bonjour,\n\n" +
                "Votre code de vérification est : " + code + "\n\n" +
                "Ce code expire dans " + codeExpirationHours + " heures.\n\n" +
                "Cordialement,\n" +
                "L'équipe d'administration");
        mailSender.send(message);
    }
}