package com.example.SGHS4.service;

import com.example.SGHS4.dto.AuthentificationDTO;
import com.example.SGHS4.dto.PendingPersonnelDTO;
import com.example.SGHS4.dto.RegistrationCompletionDTO;
import com.example.SGHS4.dto.UtilisateurDTO;
import com.example.SGHS4.entite.Validation;
import com.example.SGHS4.repository.ValidationRepository;

import com.example.SGHS4.enums.TypeDeRole;
import com.example.SGHS4.entite.PendingPersonnel;
import com.example.SGHS4.entite.Utilisateur;
import com.example.SGHS4.enums.TypeValidation;
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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern TELEPHONE_PATTERN = Pattern.compile("^[0-9]{10,}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");

    private final PendingPersonnelRepository pendingPersonnelRepository;
    private final UtilisateurRepository utilisateurRepository;
    @Autowired

    private ValidationRepository validationRepository;

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
                        JwtService jwtService,
                        ValidationRepository validationRepository) {
        this.pendingPersonnelRepository = pendingPersonnelRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.validationRepository = validationRepository; // Vérifiez que cela est bien injecté

    }

    @Transactional
    public String registerPersonnel(PendingPersonnelDTO dto) {
        logger.info("Tentative d'enregistrement d'un nouveau personnel avec email: {}", dto.getEmail());
        validatePersonnelData(dto);
        checkDuplicateData(dto);

        // Enregistrement du personnel en attente
        PendingPersonnel pending = new PendingPersonnel();
        pending.setCni(dto.getCni());
        pending.setNom(dto.getNom());
        pending.setTelephone(dto.getTelephone());
        pending.setEmail(dto.getEmail());
        pending.setRole(dto.getRoleEnum());

        pendingPersonnelRepository.save(pending);

        // Génération du code de validation
        String code = generateCode(); // Implémente cette méthode selon ton format
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(3600); // Exemple : 1 heure de validité

        Validation validation = new Validation();
        validation.setCode(code);
        validation.setCreation(now);
        validation.setExpiration(expiration);
        validation.setType(TypeValidation.ACTIVATION);
        validation.setPendingPersonnel(pending);
        validationRepository.save(validation);

        try {
            sendVerificationEmail(dto.getEmail(), code, dto.getCni());
            logger.info("Personnel enregistré et code de vérification envoyé à: {}", dto.getEmail());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email de vérification: {}", e.getMessage());
            throw new ValidationException("Personnel enregistré mais erreur lors de l'envoi du code. Contactez l'administrateur.");
        }

        return "Utilisateur enregistré avec succès. Un email avec un code de vérification a été envoyé.";
    }

    private String generateCode() {
        // Exemple de génération de code aléatoire
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @Transactional
    public String verifyAndCompleteRegistration(RegistrationCompletionDTO dto) {
        String cni = dto.getCni();
        String code = dto.getVerificationCode();
        String password = dto.getPassword();

        logger.info("Tentative de finalisation d'inscription pour CNI: {}", cni);

        try {
            // Recherche du personnel en attente
            PendingPersonnel pending = pendingPersonnelRepository.findByCni(cni)
                    .orElseThrow(() -> {
                        logger.warn("Personnel non trouvé pour CNI: {}", cni);
                        return new ValidationException("Personnel non trouvé.");
                    });

            // Validation du code de vérification

            // Validation du mot de passe via regex
            if (!PASSWORD_PATTERN.matcher(password).matches()) {
                logger.error("Mot de passe invalide pour CNI: {}", cni);
                throw new ValidationException(
                        "Le mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule, un chiffre et un caractère spécial.");
            }

            // Vérification des doublons de téléphone
            if (utilisateurRepository.existsByTelephone(pending.getTelephone())) {
                logger.warn("Numéro de téléphone déjà utilisé pour CNI: {}", cni);
                throw new UtilisateurExisteDejaException("Ce numéro de téléphone est déjà utilisé.");
            }

            // Vérification des doublons d'email
            if (utilisateurRepository.existsByEmail(pending.getEmail())) {
                logger.warn("Email déjà utilisé pour CNI: {}", cni);
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

            utilisateurRepository.save(utilisateur);
            logger.info("Utilisateur créé avec succès pour email: {}", utilisateur.getEmail());

            // Suppression du personnel en attente
            pendingPersonnelRepository.delete(pending);
            logger.info("Personnel en attente supprimé pour CNI: {}", cni);

        } catch (ValidationException | UtilisateurExisteDejaException e) {
            logger.error("Erreur fonctionnelle lors de l'inscription pour CNI: {}", cni, e);
            throw e;
        } catch (Exception e) {
            logger.error("Erreur interne lors de l'inscription pour CNI: {}", cni, e);
            throw new RuntimeException("Une erreur s'est produite lors de la finalisation de l'inscription.");
        }

        logger.info("Inscription réussie pour CNI: {}", cni);
        return "Inscription réussie et utilisateur créé.";
    }




    @Transactional
    public Map<String, String> connexion(AuthentificationDTO dto) {
        Utilisateur u = utilisateurRepository.findByEmail(dto.email())
                .orElseThrow(() -> new UsernameNotFoundException("Aucun utilisateur ne correspond à cet e-mail."));

        if (!u.isActif()) {
            throw new UtilisateurInactifException("Compte non activé.");
        }

        if (!u.getRole().equals(TypeDeRole.ADMINISTRATEUR)) {
            throw new BadCredentialsException("Vous n'avez pas les droits d'administrateur.");
        }

        if (!passwordEncoder.matches(dto.password(), u.getMdp())) {
            throw new BadCredentialsException("Identifiants incorrects.");
        }

        return jwtService.generate(u.getEmail());
    }

    public PendingPersonnel findPendingByCni(String cni) {
        return pendingPersonnelRepository.findByCni(cni)
                .orElseThrow(() -> new ValidationException("Aucun personnel temporaire avec cette CNI."));
    }
    public Utilisateur findByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Aucun utilisateur ne correspond à cet e-mail."));
    }



    @Transactional
    public void resetVerificationCode(String cni) {
        PendingPersonnel pending = pendingPersonnelRepository.findByCni(cni)
                .orElseThrow(() -> new ValidationException("Aucun personnel temporaire avec cette CNI."));

        String newCode = generateVerificationCode();

        pendingPersonnelRepository.save(pending);
        sendVerificationEmail(pending.getEmail(), newCode, pending.getCni());
    }

    private void validatePersonnelData(PendingPersonnelDTO dto) {
        if (!EMAIL_PATTERN.matcher(dto.getEmail()).matches()) {
            throw new ValidationException("Format d'email invalide.");
        }
        if (!TELEPHONE_PATTERN.matcher(dto.getTelephone()).matches()) {
            throw new ValidationException("Format de numéro de téléphone invalide.");
        }
    }

    private void checkDuplicateData(PendingPersonnelDTO dto) {
        if (utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new UtilisateurExisteDejaException("Un utilisateur avec cet email existe déjà.");
        }
        if (utilisateurRepository.existsByTelephone(dto.getTelephone())) {
            throw new UtilisateurExisteDejaException("Un utilisateur avec ce téléphone existe déjà.");
        }
        if (utilisateurRepository.existsByCni(dto.getCni())) {
            throw new UtilisateurExisteDejaException("Un utilisateur avec cette CNI existe déjà.");
        }
        if (pendingPersonnelRepository.existsByEmail(dto.getEmail())) {
            throw new UtilisateurExisteDejaException("Un personnel temporaire avec cet email existe déjà.");
        }
        if (pendingPersonnelRepository.existsByTelephone(dto.getTelephone())) {
            throw new UtilisateurExisteDejaException("Un personnel temporaire avec ce téléphone existe déjà.");
        }
        if (pendingPersonnelRepository.existsByCni(dto.getCni())) {
            throw new UtilisateurExisteDejaException("Un personnel temporaire avec cette CNI existe déjà.");
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    // Mise à jour : méthode modifiée pour inclure le lien front-end dans l'e-mail
    private void sendVerificationEmail(String to, String code, String cni) {
        String frontendLink = "http://localhost:5173/CompleteRegistration?code=" + code;

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Code de vérification - Système de Gestion Hospitalière");
            message.setText(
                    "Bonjour,\n\n" +
                            "Votre code de vérification est : " + code + "\n" +
                            "Ce code expire dans " + codeExpirationHours + " heures.\n\n" +
                            "Pour finaliser votre inscription, veuillez cliquer sur le lien suivant :\n" +
                            frontendLink + "\n\n" +
                            "Cordialement,\n" +
                            "L'équipe d'administration"
            );

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<UtilisateurDTO> getAllPersonnel() {
        List<TypeDeRole> roles = List.of(TypeDeRole.MEDECIN, TypeDeRole.INFIRMIER, TypeDeRole.LABORANTIN);

        List<Utilisateur> personnels = utilisateurRepository.findByRoleIn(roles);

        return personnels.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    public List<UtilisateurDTO> searchPersonnel(String keyword) {
        List<TypeDeRole> roles = List.of(TypeDeRole.MEDECIN, TypeDeRole.INFIRMIER, TypeDeRole.LABORANTIN);

        List<Utilisateur> personnels = utilisateurRepository.searchByRoleAndKeyword(roles, keyword);

        return personnels.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }



    private UtilisateurDTO convertToDTO(Utilisateur utilisateur) {
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setId(utilisateur.getId());
        dto.setNom(utilisateur.getNom());
        dto.setEmail(utilisateur.getEmail());
        dto.setTelephone(utilisateur.getTelephone());
        dto.setCni(utilisateur.getCni()); // ✅ AJOUT ICI


        dto.setRole(utilisateur.getRole());
        return dto;
    }


}

