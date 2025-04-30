package com.example.SGHS4.service;

import com.example.SGHS4.dto.PersonnelDTO;
import com.example.SGHS4.enums.TypeDeRole;
import com.example.SGHS4.entite.Role;
import com.example.SGHS4.entite.Utilisateur;
import com.example.SGHS4.entite.Validation;
import com.example.SGHS4.repository.RoleRepository;
import com.example.SGHS4.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class UtilisateurService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ValidationService validationService;
    private final RoleRepository roleRepository;

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

    // Inscription depuis un DTO
    public void inscription(PersonnelDTO dto) {
        if (dto.getEmail() == null || !dto.getEmail().contains("@") || !dto.getEmail().contains(".")) {
            throw new RuntimeException("L'adresse e-mail est invalide.");
        }

        if (utilisateurRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Cet e-mail est déjà utilisé.");
        }

        if (utilisateurRepository.existsByTelephone(dto.getTelephone())) {
            throw new RuntimeException("Ce numéro de téléphone est déjà utilisé.");
        }

        if (utilisateurRepository.existsByCni(dto.getCni())) {
            throw new RuntimeException("Ce numéro de CNI est déjà utilisé.");
        }

        TypeDeRole roleEnum = dto.getRoleEnum();
        if (roleEnum == null) {
            throw new RuntimeException("Rôle non valide.");
        }

        Role role = roleRepository.findByLibelle(roleEnum)
                .orElseThrow(() -> new RuntimeException("Rôle '" + roleEnum + "' non trouvé dans la base de données."));

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setNom(dto.getNom());
        utilisateur.setTelephone(dto.getTelephone());
        utilisateur.setCni(dto.getCni());  // Ajout du champ CNI
        utilisateur.setMdp(passwordEncoder.encode(dto.getTelephone())); // par défaut : téléphone = mdp
        utilisateur.setRole(roleEnum);

        utilisateurRepository.save(utilisateur);

        validationService.enregistrer(utilisateur);
    }

    // Activation d’un utilisateur via un code
    public void activation(Map<String, String> activation) {
        Validation validation = validationService.lireEnFonctionDuCode(activation.get("code"));

        if (Instant.now().isAfter(validation.getExpiration())) {
            throw new RuntimeException("Le code de validation a expiré.");
        }

        Utilisateur utilisateur = utilisateurRepository
                .findById(validation.getUtilisateur().getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé."));

        utilisateur.setActif(true);
        utilisateurRepository.save(utilisateur);
    }

    // Chargement d’un utilisateur via l'e-mail (pour Spring Security)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return utilisateurRepository
                .findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Aucun utilisateur ne correspond à cet e-mail."));
    }
}
