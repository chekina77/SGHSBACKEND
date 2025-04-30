package com.example.SGHS4.repository;

import com.example.SGHS4.entite.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);
    Optional<Utilisateur> findByCni(String cni);

    boolean existsByCni(String cni);
    // Recherche un utilisateur par son email

    boolean existsByTelephone(String telephone);  // Vérifie si le téléphone existe déjà
    boolean existsByEmail(String email);  // Vérifie si l'email existe déjà

}
