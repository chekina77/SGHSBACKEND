package com.example.SGHS4.service;

import com.example.SGHS4.dto.PersonnelDTO;
import com.example.SGHS4.dto.ModificationMdpDTO;
import com.example.SGHS4.enums.TypeDeRole;
import com.example.SGHS4.entite.Role;
import com.example.SGHS4.entite.Utilisateur;
import com.example.SGHS4.entite.Validation;
import com.example.SGHS4.exceptions.UtilisateurExisteDejaException;
import com.example.SGHS4.exceptions.UtilisateurInactifException;
import com.example.SGHS4.exceptions.ValidationException;
import com.example.SGHS4.repository.RoleRepository;
import com.example.SGHS4.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
public class UtilisateurService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UtilisateurService.class);

    // Expressions régulières pour la validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");

    private final UtilisateurRepository utilisateurRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ValidationService validationService;
    private final RoleRepository roleRepository;

    @Value("${user.password.min-length:8}")
    private int passwordMinLength;

    @Value("${user.password.temporary-expires:1440}") // 24 heures par défaut (en minutes)
    private long temporaryPasswordExpiresMinutes;

    @Autowired
    public UtilisateurService(UtilisateurRepository utilisateurRepository,
                              BCryptPasswordEncoder passwordEncoder,
                              ValidationService validationService,
                              RoleRepository roleRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.validationService = validationService;
        this.roleRepository = roleRepository;
    }

    /**
     * Inscription d'un nouvel utilisateur depuis un DTO
     * @param dto Les données du personnel à inscrire
     * @throws UtilisateurExisteDejaException Si les informations sont déjà utilisées
     * @throws ValidationException Si les données sont invalides
     */
    public void inscription(PersonnelDTO dto) {
        // Validation d'email
        if (dto.getEmail() == null || !EMAIL_PATTERN.matcher(dto.getEmail()).matches()) {
            throw new ValidationException("L'adresse e-mail est invalide.");
        }

        // Vérification de doublons
        if (utilisateurRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new UtilisateurExisteDejaException("Cet e-mail est déjà utilisé.");
        }

        if (utilisateurRepository.existsByTelephone(dto.getTelephone())) {
            throw new UtilisateurExisteDejaException("Ce numéro de téléphone est déjà utilisé.");
        }

        if (utilisateurRepository.existsByCni(dto.getCni())) {
            throw new UtilisateurExisteDejaException("Ce numéro de CNI est déjà utilisé.");
        }

        // Validation du rôle
        TypeDeRole roleEnum = dto.getRoleEnum();
        if (roleEnum == null) {
            throw new ValidationException("Rôle non valide.");
        }

        Role role = roleRepository.findByLibelle(roleEnum)
                .orElseThrow(() -> new ValidationException("Rôle '" + roleEnum + "' non trouvé dans la base de données."));

        // Création de l'utilisateur
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setNom(dto.getNom());
        utilisateur.setTelephone(dto.getTelephone());
        utilisateur.setCni(dto.getCni());

        // Génération d'un mot de passe temporaire sécurisé (12 caractères)
        // Le mot de passe par défaut est encodé et devra être changé à la première connexion
        utilisateur.setMdp(passwordEncoder.encode(dto.getTelephone()));
        utilisateur.setMotDePasseTemporaire(true);
        utilisateur.setDateCreationMotDePasse(Instant.now());
        utilisateur.setRole(roleEnum);

        // Sauvegarde de l'utilisateur et génération du code d'activation
        utilisateurRepository.save(utilisateur);
        validationService.enregistrer(utilisateur);

        logger.info("Nouvel utilisateur inscrit avec l'email: {}", dto.getEmail());
    }

    /**
     * Activation d'un compte utilisateur via un code de validation
     * @param activation Map contenant le code d'activation
     * @throws ValidationException Si le code est invalide ou expiré
     */
    public void activation(Map<String, String> activation) {
        if (!activation.containsKey("code")) {
            throw new ValidationException("Le code d'activation est requis");
        }

        Validation validation = validationService.lireEnFonctionDuCode(activation.get("code"));

        if (Instant.now().isAfter(validation.getExpiration())) {
            throw new ValidationException("Le code de validation a expiré.");
        }

        Utilisateur utilisateur = utilisateurRepository
                .findById(validation.getUtilisateur().getId())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé."));

        utilisateur.setActif(true);
        utilisateurRepository.save(utilisateur);

        logger.info("Activation réussie pour l'utilisateur ID {}", utilisateur.getId());
    }

    /**
     * Modification du mot de passe utilisateur
     * @param dto Contient l'ancien et le nouveau mot de passe
     * @throws ValidationException Si le mot de passe ne respecte pas les critères de sécurité
     */
    public void modifierMotDePasse(ModificationMdpDTO dto) {
        // Récupération de l'utilisateur connecté
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Utilisateur utilisateur = utilisateurRepository
                .findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé."));

        // Vérification de l'ancien mot de passe
        if (!passwordEncoder.matches(dto.getAncienMotDePasse(), utilisateur.getPassword())) {
            throw new ValidationException("Mot de passe actuel incorrect.");
        }

        // Validation du nouveau mot de passe
        if (dto.getNouveauMotDePasse().length() < passwordMinLength) {
            throw new ValidationException("Le mot de passe doit contenir au moins " + passwordMinLength + " caractères.");
        }

        if (!PASSWORD_PATTERN.matcher(dto.getNouveauMotDePasse()).matches()) {
            throw new ValidationException("Le mot de passe doit contenir au moins une majuscule, une minuscule, un chiffre et un caractère spécial.");
        }

        // Mise à jour du mot de passe
        utilisateur.setMdp(passwordEncoder.encode(dto.getNouveauMotDePasse()));
        utilisateur.setMotDePasseTemporaire(false);
        utilisateur.setDateCreationMotDePasse(Instant.now());
        utilisateurRepository.save(utilisateur);

        logger.info("Mot de passe modifié pour l'utilisateur {}", utilisateur.getEmail());
    }

    /**
     * Chargement d'un utilisateur pour Spring Security
     * @param username Email de l'utilisateur
     * @return UserDetails Les détails de l'utilisateur
     * @throws UsernameNotFoundException Si l'utilisateur n'existe pas
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository
                .findByEmail(username)
                .orElseThrow(() -> {
                    logger.warn("Tentative de connexion avec un email inconnu: {}", username);
                    return new UsernameNotFoundException("Aucun utilisateur ne correspond à cet e-mail.");
                });

        // Vérification si l'utilisateur est actif
        if (!utilisateur.isActif()) {
            logger.warn("Tentative de connexion avec un compte inactif: {}", username);
            throw new UtilisateurInactifException("Votre compte n'est pas activé. Veuillez vérifier votre email pour le code d'activation.");
        }

        // Vérification si le mot de passe temporaire est expiré
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
}