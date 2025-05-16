package com.example.SGHS4.service;

import com.example.SGHS4.dto.DoctorDTO;
import com.example.SGHS4.dto.PendingPersonnelDTO;
import com.example.SGHS4.dto.ModificationMdpDTO;
import com.example.SGHS4.entite.*;
import com.example.SGHS4.enums.TypeDeRole;
import com.example.SGHS4.exceptions.ResourceNotFoundException;
import com.example.SGHS4.exceptions.UtilisateurExisteDejaException;
import com.example.SGHS4.exceptions.UtilisateurInactifException;
import com.example.SGHS4.exceptions.ValidationException;
import com.example.SGHS4.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class UtilisateurService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UtilisateurService.class);

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");

    private final UtilisateurRepository utilisateurRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ValidationService validationService;
    private final RoleRepository roleRepository;
    private final PendingPersonnelRepository pendingPersonnelRepository;
    private final EmailService emailService;
    private final AppointementDoctorRepository appointementDoctorRepository;


    private ValidationRepository validationRepository;

    private CodeReinitialisationRepository codeReinitialisationRepository;
    private static final long EXPIRATION_HOURS = 24L;

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private JwtRepository jwtRepository;


    @Value("${user.password.min-length:8}")
    private int passwordMinLength;
    @Value("${validation.code.expiration-hours:24}")
    private int codeExpirationHours;
    @Value("${user.password.temporary-expires:1440}")
    private long temporaryPasswordExpiresMinutes;

    @Autowired
    public UtilisateurService(UtilisateurRepository utilisateurRepository,
                              BCryptPasswordEncoder passwordEncoder,
                              ValidationService validationService,
                              RoleRepository roleRepository,
                              PendingPersonnelRepository pendingPersonnelRepository,
                              EmailService emailService,
                              ValidationRepository validationRepository,
                              CodeReinitialisationRepository codeReinitialisationRepository,
                              JavaMailSender mailSender,AppointementDoctorRepository appointementDoctorRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.validationService = validationService;
        this.roleRepository = roleRepository;
        this.pendingPersonnelRepository = pendingPersonnelRepository;
        this.emailService = emailService;
        this.validationRepository = validationRepository;
        this.codeReinitialisationRepository = codeReinitialisationRepository;
        this.mailSender = mailSender; // <-- et ici
        this.appointementDoctorRepository = appointementDoctorRepository;



    }

    public List<DoctorDTO> getNomsDesMedecins() {
        return utilisateurRepository.findByRole(TypeDeRole.MEDECIN)
                .stream()
                .map(u -> new DoctorDTO(u.getId(), u.getNom()))
                .collect(Collectors.toList());
    }


    public void inscription(PendingPersonnelDTO dto) {
        if (dto.getEmail() == null || !EMAIL_PATTERN.matcher(dto.getEmail()).matches()) {
            throw new ValidationException("L'adresse e-mail est invalide.");
        }

        if (utilisateurRepository.findByEmail(dto.getEmail()).isPresent() ||
                pendingPersonnelRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new UtilisateurExisteDejaException("Cet e-mail est déjà utilisé.");
        }

        if (utilisateurRepository.existsByTelephone(dto.getTelephone()) ||
                pendingPersonnelRepository.existsByTelephone(dto.getTelephone())) {
            throw new UtilisateurExisteDejaException("Ce numéro de téléphone est déjà utilisé.");
        }

        if (utilisateurRepository.existsByCni(dto.getCni()) ||
                pendingPersonnelRepository.existsByCni(dto.getCni())) {
            throw new UtilisateurExisteDejaException("Ce numéro de CNI est déjà utilisé.");
        }

        TypeDeRole roleEnum = dto.getRoleEnum();
        if (roleEnum == null) {
            throw new ValidationException("Rôle non valide.");
        }

        Role role = roleRepository.findByLibelle(roleEnum)
                .orElseThrow(() -> new ValidationException("Rôle '" + roleEnum + "' non trouvé dans la base de données."));

        // Créer un nouvel objet PendingPersonnel
        PendingPersonnel pending = new PendingPersonnel();
        pending.setNom(dto.getNom());
        pending.setEmail(dto.getEmail());
        pending.setTelephone(dto.getTelephone());
        pending.setCni(dto.getCni());
        pending.setRole(roleEnum);

        // Générer le code d'activation
        String codeActivation = validationService.genererCode();

        // Créer un objet Validation
        Validation validation = new Validation();
        validation.setCode(codeActivation);
        validation.setCreation(Instant.now());

        // Définir la date d'expiration du code
        LocalDateTime expiryDateTime = LocalDateTime.ofInstant(Instant.now().plus(Duration.ofHours(codeExpirationHours)), ZoneId.systemDefault());
        validation.setExpiration(expiryDateTime.toInstant(ZoneOffset.UTC));

        // Lier la validation au personnel en attente
        validation.setPendingPersonnel(pending);

        // Sauvegarder l'entité PendingPersonnel
        pendingPersonnelRepository.save(pending);

        // Sauvegarder l'entité Validation
        validationRepository.save(validation);

        // Envoyer l'email avec le code d'activation
        emailService.envoyerCodeActivation(dto.getEmail(), codeActivation);

        logger.info("Inscription en attente enregistrée pour {}", dto.getEmail());
    }

    public void activation(Map<String, String> activation) {
        if (!activation.containsKey("code") || !activation.containsKey("motDePasse")) {
            throw new ValidationException("Le code d'activation et le mot de passe sont requis.");
        }

        String code = activation.get("code");
        String motDePasse = activation.get("motDePasse");

        Validation validation = validationService.lireEnFonctionDuCode(code);
        if (Instant.now().isAfter(validation.getExpiration())) {
            throw new ValidationException("Le code de validation a expiré.");
        }

        PendingPersonnel pending = validation.getPendingPersonnel();

        if (pending == null) {
            throw new ResourceNotFoundException("Aucun utilisateur en attente associé à ce code.");
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(pending.getNom());
        utilisateur.setEmail(pending.getEmail());
        utilisateur.setTelephone(pending.getTelephone());
        utilisateur.setCni(pending.getCni());
        utilisateur.setRole(pending.getRole());
        utilisateur.setMdp(passwordEncoder.encode(motDePasse));
        utilisateur.setActif(true);
        utilisateur.setMotDePasseTemporaire(false);
        utilisateur.setDateCreationMotDePasse(Instant.now());

        utilisateurRepository.save(utilisateur);
        pendingPersonnelRepository.delete(pending);
        validationService.supprimer(validation);

        logger.info("Activation réussie pour l'email {}", utilisateur.getEmail());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository
                .findByEmail(username)
                .orElseThrow(() -> {
                    logger.warn("Tentative de connexion avec un email inconnu: {}", username);
                    return new UsernameNotFoundException("Aucun utilisateur ne correspond à cet e-mail.");
                });

        if (!utilisateur.isActif()) {
            logger.warn("Tentative de connexion avec un compte inactif: {}", username);
            throw new UtilisateurInactifException("Votre compte n'est pas activé. Veuillez vérifier votre email pour le code d'activation.");
        }

        if (utilisateur.isMotDePasseTemporaire()) {
            Instant passwordCreation = utilisateur.getDateCreationMotDePasse();
            if (passwordCreation != null &&
                    passwordCreation.plusSeconds(temporaryPasswordExpiresMinutes * 60).isBefore(Instant.now())) {
                logger.warn("Tentative de connexion avec un mot de passe temporaire expiré: {}", username);
                throw new ValidationException("Votre mot de passe temporaire a expiré. Veuillez contacter l'administrateur.");
            }
        }

        return utilisateur;
    }

    public void envoyerCodeReinitialisation(String email) {
        // Générer un code aléatoire de 6 chiffres
        String code = genererCode();

        // Créer ou récupérer le PendingPersonnel associé à cet email
        PendingPersonnel pending = pendingPersonnelRepository.findByEmail(email).orElse(null);
        if (pending == null) {
            pending = new PendingPersonnel();
            pending.setEmail(email);
        }

        // Créer l'objet Validation avec une expiration de 24 heures
        Validation validation = new Validation();
        validation.setCode(code);
        validation.setExpiration(Instant.now().plus(EXPIRATION_HOURS, ChronoUnit.HOURS));
        validation.setPendingPersonnel(pending);

        // Sauvegarder le code dans la base de données
        validationRepository.save(validation);

        // Envoi par email
        emailService.envoyerCodeReinitialisation(email, code);
        logger.info("Code de réinitialisation généré et envoyé à {}", email);
    }

    /**
     * Envoie un code de réinitialisation fourni en paramètre à l'utilisateur par email.
     * À utiliser si le code a déjà été créé ailleurs (ex: renvoi d'un code existant).
     */
    public void envoyerCodeReinitialisation(String email, String code) {
        emailService.envoyerCodeReinitialisation(email, code);
        logger.info("Code de réinitialisation envoyé à {}", email);
    }

    public void modifierMotDePasse(ModificationMdpDTO dto) {
        if (!dto.getNouveauMotDePasse().equals(dto.getConfirmationNouveauMotDePasse())) {
            throw new ValidationException("Les mots de passe ne correspondent pas.");
        }

        Validation validation = validationRepository.findByCode(dto.getCodeReinitialisation())
                .orElseThrow(() -> new ValidationException("Code invalide."));

        if (validation.getExpiration().isBefore(Instant.now())) {
            throw new ValidationException("Code expiré.");
        }

        if (!validation.getPendingPersonnel().getEmail().equalsIgnoreCase(dto.getEmail())) {
            throw new ValidationException("L'email ne correspond pas.");
        }

        if (dto.getNouveauMotDePasse().length() < passwordMinLength) {
            throw new ValidationException("Le mot de passe doit contenir au moins " + passwordMinLength + " caractères.");
        }

        if (!PASSWORD_PATTERN.matcher(dto.getNouveauMotDePasse()).matches()) {
            throw new ValidationException("Le mot de passe doit contenir au moins une majuscule, une minuscule, un chiffre et un caractère spécial.");
        }

        Utilisateur utilisateur = utilisateurRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé."));

        utilisateur.setMdp(passwordEncoder.encode(dto.getNouveauMotDePasse()));
        utilisateur.setMotDePasseTemporaire(false);
        utilisateur.setDateCreationMotDePasse(Instant.now());
        utilisateurRepository.save(utilisateur);

        validationRepository.delete(validation);

        logger.info("Mot de passe mis à jour pour {}", dto.getEmail());
    }

    public void verifierEtEnvoyerNouveauCode(String email) {
        // Vérifier que l'utilisateur existe
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email : " + email));

        // Désactiver les anciens codes encore actifs pour cet utilisateur
        List<CodeReinitialisation> anciensCodes = codeReinitialisationRepository.findByUtilisateurAndActifTrue(utilisateur);
        for (CodeReinitialisation code : anciensCodes) {
            code.setActif(false);
        }
        codeReinitialisationRepository.saveAll(anciensCodes);

        // Générer un nouveau code
        String nouveauCode = genererCode();

        // Créer un nouveau code de réinitialisation
        CodeReinitialisation nouveauCodeReinitialisation = new CodeReinitialisation();
        nouveauCodeReinitialisation.setUtilisateur(utilisateur);
        nouveauCodeReinitialisation.setCode(nouveauCode);
        nouveauCodeReinitialisation.setExpirationDate(Instant.now().plus(EXPIRATION_HOURS, ChronoUnit.HOURS));
        nouveauCodeReinitialisation.setActif(true);

        codeReinitialisationRepository.save(nouveauCodeReinitialisation);

        // Envoyer le code
        envoyerCodeReinitialisation(email, nouveauCode);
    }


    private String genererCode() {
        // Génère un code aléatoire de 6 chiffres
        int code = (int) (Math.random() * 900000) + 100000; // entre 100000 et 999999
        return String.valueOf(code);
    }

    public Utilisateur findById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID : " + id));
    }

    // Méthode pour supprimer un utilisateur
    public void supprimerUtilisateur(Long utilisateurId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID : " + utilisateurId));

        // Vérification de rendez-vous associés
        List<AppointementDoctor> rendezVous = appointementDoctorRepository.findByUtilisateur(utilisateur);
        if (!rendezVous.isEmpty()) {
            throw new RuntimeException("Impossible de supprimer cet utilisateur : des rendez-vous lui sont associés.");
        }

        // Suppression
        utilisateurRepository.delete(utilisateur);
    }
    public void updateUtilisateur(Long id, PendingPersonnelDTO dto) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + id));

        // Vérifications optionnelles si on souhaite empêcher les doublons
        if (!utilisateur.getEmail().equals(dto.getEmail()) && utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalStateException("Cet email est déjà utilisé.");
        }

        if (!utilisateur.getTelephone().equals(dto.getTelephone()) && utilisateurRepository.existsByTelephone(dto.getTelephone())) {
            throw new IllegalStateException("Ce numéro de téléphone est déjà utilisé.");
        }

        if (!utilisateur.getCni().equals(dto.getCni()) && utilisateurRepository.existsByCni(dto.getCni())) {
            throw new IllegalStateException("Ce numéro de carte nationale d'identité est déjà utilisé.");
        }

        // Mise à jour des champs
        utilisateur.setNom(dto.getNom());
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setTelephone(dto.getTelephone());
        utilisateur.setCni(dto.getCni());

        utilisateurRepository.save(utilisateur);
    }

}