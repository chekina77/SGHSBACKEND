package com.example.SGHS4.repository;

import com.example.SGHS4.entite.Utilisateur;
import com.example.SGHS4.entite.Validation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ValidationRepository extends JpaRepository<Validation, Long> {

    /**
     * Trouve une validation par son code
     */
    Optional<Validation> findByCode(String code);

    /**
     * Trouve une validation par son code et son statut d'activation
     */
    Optional<Validation> findByCodeAndActif(String code, boolean actif);

    /**
     * Trouve toutes les validations d'un utilisateur avec un statut d'activation spécifique
     */
    List<Validation> findByUtilisateurAndActif(Utilisateur utilisateur, boolean actif);

    /**
     * Trouve toutes les validations expirées avant une date donnée
     */
    List<Validation> findByExpirationBefore(Instant date);

    /**
     * Trouve un utilisateur par son email (utilisé pour la régénération de code)
     */
    @Query("SELECT v.utilisateur FROM Validation v WHERE v.utilisateur.email = :email ORDER BY v.creation DESC")
    Optional<Utilisateur> findUtilisateurByEmail(@Param("email") String email);

    /**
     * Trouve la dernière validation d'un utilisateur
     */
    @Query("FROM Validation v WHERE v.utilisateur.id = :utilisateurId ORDER BY v.creation DESC")
    Optional<Validation> findLastByUtilisateurId(@Param("utilisateurId") Long utilisateurId);

    /**
     * Compte le nombre de codes de validation générés pour un utilisateur dans un intervalle de temps
     */
    @Query("SELECT COUNT(v) FROM Validation v WHERE v.utilisateur.id = :utilisateurId AND v.creation > :depuis")
    long countByUtilisateurIdAndCreationAfter(
            @Param("utilisateurId") Long utilisateurId,
            @Param("depuis") Instant depuis
    );
}