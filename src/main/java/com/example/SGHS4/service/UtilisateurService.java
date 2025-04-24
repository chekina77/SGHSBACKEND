package com.example.SGHS4.service;

import com.example.SGHS4.TypeDeRole;
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
import java.util.Optional;

@Service
public class UtilisateurService implements UserDetailsService {

    private UtilisateurRepository utilisateurRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private ValidationService validationService;
    private RoleRepository roleRepository;

    public UtilisateurService(UtilisateurRepository utilisateurRepository, BCryptPasswordEncoder passwordEncoder, ValidationService validationService, RoleRepository roleRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.validationService = validationService;
        this.roleRepository = roleRepository;
    }

    public void inscription(Utilisateur utilisateur) {
        if (utilisateur.getEmail() == null ||
                !utilisateur.getEmail().contains("@") ||
                !utilisateur.getEmail().contains(".")) {
            throw new RuntimeException("Votre mail est invalide");
        }

        Optional<Utilisateur> utilisateurOptional = utilisateurRepository.findByEmail(utilisateur.getEmail());

        if (utilisateurOptional.isPresent()) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        String mdpCrypte = passwordEncoder.encode(utilisateur.getMdp());
        utilisateur.setMdp(mdpCrypte);
        Role roleUtilisateur = new Role();
        roleUtilisateur.setLibelle(String.valueOf(TypeDeRole.UTILISATEUR));
        Role role= roleRepository.save(roleUtilisateur);
        utilisateur.setRole(role);
        utilisateur.setPassword(mdpCrypte);

        utilisateur = utilisateurRepository.save(utilisateur);
        validationService.enregistrer(utilisateur);
    }

    public void activation(Map<String, String> activation) {
        Validation validation = this.validationService.lireEnFonctionDuCode(activation.get("code"));

        if (Instant.now().isAfter(validation.getExpiration())) {
            throw new RuntimeException("Votre code a expiré");
        }

        Utilisateur utilisateur = this.utilisateurRepository
                .findById(validation.getUtilisateur().getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur inconnu"));

        utilisateur.setActif(true);
        utilisateurRepository.save(utilisateur);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utilisateur utilisateur = this.utilisateurRepository
                .findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Aucun utilisateur ne correspond à cet identifiant"));

        return utilisateur;
    }
    public void RegisterUser(Utilisateur utilisateur) {
        if (utilisateur.getEmail() == null ||
                !utilisateur.getEmail().contains("@") ||
                !utilisateur.getEmail().contains(".")) {
            throw new RuntimeException("Votre mail est invalide");
        }

        Optional<Utilisateur> utilisateurOptional = utilisateurRepository.findByEmail(utilisateur.getEmail());

        if (utilisateurOptional.isPresent()) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }
    }

}