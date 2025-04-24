package com.example.SGHS4.Seeder;

import com.example.SGHS4.TypeDeRole;
import com.example.SGHS4.entite.Role;
import com.example.SGHS4.entite.Utilisateur;
import com.example.SGHS4.repository.RoleRepository;
import com.example.SGHS4.repository.UtilisateurRepository;
import com.example.SGHS4.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CreateAdminSeeder implements CommandLineRunner {

    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private ValidationService validationService;
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {

        if (utilisateurRepository.existsByEmail("fossiechekina@gmail.com")) {
            System.out.println("Administrateur déjà existant.");
            return;
        }

        Utilisateur utilisateur = new Utilisateur();
        String mdpCrypte = passwordEncoder.encode("xoxo");
        utilisateur.setMdp(mdpCrypte);

        Role roleUtilisateur = new Role();
        roleUtilisateur.setLibelle(String.valueOf(TypeDeRole.ADMINISTRATEUR));
        Role role = roleRepository.save(roleUtilisateur);

        utilisateur.setRole(roleUtilisateur);
        utilisateur.setPassword(mdpCrypte);
        utilisateur.setNom("Admin");
        utilisateur.setEmail("fossiechekina@gmail.com");
        utilisateur.setActif(true);

        Utilisateur utilisateurSaved = utilisateurRepository.save(utilisateur);
        validationService.enregistrer(utilisateurSaved);
    }

}