package com.example.SGHS4.config;

import com.example.SGHS4.entite.Role;
import com.example.SGHS4.entite.Utilisateur;
import com.example.SGHS4.enums.TypeDeRole;
import com.example.SGHS4.repository.RoleRepository;
import com.example.SGHS4.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.util.Optional;

@Component
public class DataInitialize {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String email;

    @Value("${admin.password}")
    private String password;

    @Value("${admin.nom}")
    private String nom;

    @Value("${admin.telephone}")
    private String telephone;

    @Value("${admin.cni}")
    private String cni;

    public DataInitialize(UtilisateurRepository utilisateurRepository,
                          RoleRepository roleRepository,
                          BCryptPasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initAdmin() {
        // First, ensure the ADMINISTRATEUR role exists
        Role adminRole = roleRepository.findByLibelle(TypeDeRole.ADMINISTRATEUR)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setLibelle(TypeDeRole.ADMINISTRATEUR);
                    return roleRepository.save(newRole);
                });

        // Then create the admin user if it doesn't exist
        if (utilisateurRepository.findByEmail(email).isEmpty()) {
            Utilisateur admin = new Utilisateur();
            admin.setEmail(email);
            admin.setNom(nom);
            admin.setTelephone(telephone);
            admin.setCni(cni);
            admin.setActif(true);
            admin.setRole(TypeDeRole.ADMINISTRATEUR);
            admin.setMdp(passwordEncoder.encode(password));

            utilisateurRepository.save(admin);

            System.out.println("Admin initialisé avec succès !");
        } else {
            System.out.println("Admin déjà présent.");
        }
    }
}
