package com.example.SGHS4.service;

import com.example.SGHS4.dto.AuthentificationDTO;
import com.example.SGHS4.dto.PersonnelDTO;
import com.example.SGHS4.enums.TypeDeRole;
import com.example.SGHS4.entite.PendingPersonnel;
import com.example.SGHS4.entite.Utilisateur;
import com.example.SGHS4.entite.Role;
import com.example.SGHS4.repository.PendingPersonnelRepository;
import com.example.SGHS4.repository.UtilisateurRepository;
import com.example.SGHS4.repository.RoleRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AdminService {

    private final PendingPersonnelRepository pendingPersonnelRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final JavaMailSender mailSender;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

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

    public String registerPersonnel(PersonnelDTO dto) {
        if (dto == null) {
            return "Les données de l'utilisateur ne peuvent pas être nulles.";
        }
        if (isTelephoneAlreadyUsed(dto.getTelephone())) {
            return "Ce numéro de téléphone est déjà utilisé.";
        }
        if (isEmailAlreadyUsed(dto.getEmail())) {
            return "Cet email est déjà utilisé.";
        }
        if (isCniAlreadyUsed(dto.getCni())) {
            return "Un personnel avec ce numéro de CNI existe déjà.";
        }
        if (isInvalidPersonnelData(dto)) {
            return "Certains champs sont invalides. Vérifiez le nom, téléphone et email.";
        }
        TypeDeRole roleEnum = dto.getRoleEnum();
        if (roleEnum == null) {
            return "Rôle non valide.";
        }
        Optional<Role> optionalRole = roleRepository.findByLibelle(roleEnum);
        if (optionalRole.isEmpty()) {
            return "Rôle non trouvé dans la base de données.";
        }
        PendingPersonnel pending = new PendingPersonnel();
        pending.setCni(dto.getCni());
        pending.setNom(dto.getNom());
        pending.setTelephone(dto.getTelephone());
        pending.setEmail(dto.getEmail());
        pending.setRole(roleEnum);
        String code = generateVerificationCode();
        pending.setVerificationCode(code);
        pending.setVerificationCodeExpiry(LocalDateTime.now().plusDays(1));

        pendingPersonnelRepository.save(pending);
        sendVerificationEmail(dto.getEmail(), code);
        return "Utilisateur enregistré. Un email avec un code de vérification a été envoyé.";
    }
    @Value("${jwt.secret}")
    private String secretKey;

    public Map<String, String> generate(Utilisateur utilisateur) {
        // Génére un token JWT avec le rôle inclus dans les "claims"
        String bearerToken = Jwts.builder()
                .setSubject(utilisateur.getEmail())
                .claim("nom", utilisateur.getNom())    // Optionnel : le nom
                .claim("role", utilisateur.getRole())   // ✅ Important : Ajouter le rôle ici
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // +1 jour
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        // Créer un refresh token pour rafraîchir la session (optionnel)
        String refreshToken = UUID.randomUUID().toString();

        // Retourner une map avec les tokens
        Map<String, String> tokens = new HashMap<>();
        tokens.put("bearer", bearerToken);
        tokens.put("refresh", refreshToken);
        return tokens;
    }

    private boolean isTelephoneAlreadyUsed(String telephone) {
        return pendingPersonnelRepository.existsByTelephone(telephone)
                || utilisateurRepository.existsByTelephone(telephone);
    }

    private boolean isEmailAlreadyUsed(String email) {
        return pendingPersonnelRepository.existsByEmail(email)
                || utilisateurRepository.existsByEmail(email);
    }

    private boolean isCniAlreadyUsed(String cni) {
        return cni != null && !cni.isEmpty()
                && pendingPersonnelRepository.findByCni(cni).isPresent();
    }

    private boolean isInvalidPersonnelData(PersonnelDTO dto) {
        return dto.getNom() == null || dto.getNom().isEmpty()
                || dto.getTelephone() == null || dto.getTelephone().isEmpty()
                || dto.getEmail() == null || dto.getEmail().isEmpty()
                || !dto.getEmail().contains("@") || !dto.getEmail().contains(".")
                || !dto.getTelephone().matches("[0-9]+") || dto.getTelephone().length() < 10;
    }

    private String generateVerificationCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private void sendVerificationEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Code de vérification");
        message.setText("Votre code de vérification est : " + code + "\nCe code expire dans 24 heures.");
        mailSender.send(message);
    }

    @Transactional(readOnly = true)
    public PendingPersonnel findPendingByCni(String cni) {
        return pendingPersonnelRepository.findByCni(cni)
                .orElseThrow(() -> new RuntimeException("Aucun enregistrement pending pour ce CNI."));
    }

    /**
     * Vérifie le code, crée un Utilisateur, gère les doublons et supprime le PendingPersonnel.
     */
    @Transactional
    public String verifyAndCompleteRegistration(String cni, String code, String password) {
        PendingPersonnel p = pendingPersonnelRepository.findByCni(cni).orElse(null);
        if (p == null) return "Personnel non trouvé.";
        if (!p.getVerificationCode().equals(code)) return "Code de vérification incorrect.";

        // Pré-vérifications d'unicité avant le save
        if (utilisateurRepository.existsByTelephone(p.getTelephone())) return "Téléphone déjà utilisé.";
        if (utilisateurRepository.existsByEmail(p.getEmail())) return "Email déjà utilisé.";
        // on ne re-vérifie pas le CNI ici pour permettre la finalisation

        Utilisateur u = new Utilisateur();
        u.setCni(p.getCni());
        u.setNom(p.getNom());
        u.setTelephone(p.getTelephone());
        u.setEmail(p.getEmail());
        u.setRole(p.getRole());
        u.setMdp(password);  // encodé dans le setter
        u.setActif(true);
        u.setVerificationCode(code);

        // Sauvegarde et suppression dans la même transaction
        utilisateurRepository.save(u);
        pendingPersonnelRepository.delete(p);

        return "Inscription réussie et utilisateur créé.";
    }
    public Map<String, String> connexion(AuthentificationDTO dto) {
        // Rechercher l'utilisateur par email
        Utilisateur utilisateur = utilisateurRepository.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Vérifier si le compte est actif
        if (!utilisateur.isActif()) {
            throw new RuntimeException("Compte inactif. Veuillez activer votre compte.");
        }

        // Vérifier le mot de passe
        if (!passwordEncoder.matches(dto.password(), utilisateur.getMdp())) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        // Générer le token JWT
        return jwtService.generate(utilisateur.getEmail());
    }


}