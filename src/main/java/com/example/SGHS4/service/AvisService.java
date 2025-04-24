package com.example.SGHS4.service;

import com.example.SGHS4.entite.Utilisateur;
import com.example.SGHS4.repository.AvisRepository;
import com.example.SGHS4.entite.Avis;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

@Service
public class AvisService {

    private final AvisRepository avisRepository;

    public AvisService(AvisRepository avisRepository) {
        this.avisRepository = avisRepository;
    }

    public void creer(Avis avis) {
        // Ensure principal is of type Utilisateur
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof Utilisateur) {
            Utilisateur utilisateur = (Utilisateur) principal;
            avis.setUtilisateur(utilisateur); // Associate avis with the authenticated user
            this.avisRepository.save(avis); // Save avis
        } else {
            // Handle case where principal is not of type Utilisateur (optional)
            throw new IllegalStateException("User not authenticated properly");
        }
    }
}
