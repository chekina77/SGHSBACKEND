package com.example.SGHS4.repository;

import com.example.SGHS4.entite.PendingPersonnel;
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
     * Trouve une validation par son code.
     * @param code le code de validation
     * @return une validation correspondante
     */
    Optional<Validation> findByCode(String code);

    /**
     * Trouve une validation par son code et son statut d'activation.
     * @param code le code de validation
     * @param actif le statut d'activation (actif ou non)
     * @return une validation correspondante
     */
    Optional<Validation> findByCodeAndActif(String code, boolean actif);

    /**
     * Trouve toutes les validations d'un utilisateur avec un statut d'activation spécifique.
     * @param utilisateur l'utilisateur pour lequel trouver les validations
     * @param actif le statut d'activation (actif ou non)
     * @return une liste de validations
     */
    List<Validation> findByUtilisateurAndActif(Utilisateur utilisateur, boolean actif);

    /**
     * Trouve toutes les validations expirées avant une date donnée.
     * @param date la date limite d'expiration
     * @return une liste de validations expirées avant la date donnée
     */
    List<Validation> findByExpirationBefore(Instant date);

    /**
     * Trouve un utilisateur par son email (utilisé pour la régénération de code).
     * @param email l'email de l'utilisateur
     * @return un utilisateur associé à cet email
     */
    List<Validation> findByPendingPersonnelAndActif(PendingPersonnel pendingPersonnel, boolean actif);

    @Query("SELECT v.utilisateur FROM Validation v WHERE v.utilisateur.email = :email ORDER BY v.creation DESC")
    Optional<Utilisateur> findUtilisateurByEmail(@Param("email") String email);

    /**
     * Trouve la dernière validation d'un utilisateur.
     * @param utilisateurId l'id de l'utilisateur
     * @return la dernière validation associée à cet utilisateur
     */
    @Query("FROM Validation v WHERE v.utilisateur.id = :utilisateurId ORDER BY v.creation DESC")
    Optional<Validation> findLastByUtilisateurId(@Param("utilisateurId") Long utilisateurId);

    /**
     * Compte le nombre de codes de validation générés pour un utilisateur dans un intervalle de temps.
     * @param utilisateurId l'id de l'utilisateur
     * @param depuis la date de début de l'intervalle
     * @return le nombre de validations créées pour cet utilisateur depuis cette date
     */
    @Query("SELECT COUNT(v) FROM Validation v WHERE v.utilisateur.id = :utilisateurId AND v.creation > :depuis")
    long countByUtilisateurIdAndCreationAfter(
            @Param("utilisateurId") Long utilisateurId,
            @Param("depuis") Instant depuis
    );
    Optional<Validation> findByPendingPersonnel(PendingPersonnel pendingPersonnel);
    void deleteByPendingPersonnel(PendingPersonnel pendingPersonnel);

}
