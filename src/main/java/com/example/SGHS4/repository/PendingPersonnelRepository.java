package com.example.SGHS4.repository;

import com.example.SGHS4.entite.PendingPersonnel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour accéder et manipuler les données de la table PendingPersonnel.
 */
@Repository
public interface PendingPersonnelRepository extends JpaRepository<PendingPersonnel, Long> {

    /**
     * Recherche un personnel en attente par son numéro de CNI.
     *
     * @param cni Le numéro de CNI
     * @return Un Optional contenant le personnel s'il existe, sinon vide
     */
    Optional<PendingPersonnel> findByCni(String cni);

    /**
     * Vérifie si un personnel en attente existe avec le CNI donné.
     *
     * @param cni Le numéro de CNI
     * @return true si un personnel avec ce CNI existe, sinon false
     */
    boolean existsByCni(String cni);

    /**
     * Recherche un personnel en attente par email.
     *
     * @param email L'adresse email
     * @return Un Optional contenant le personnel s'il existe, sinon vide
     */
    Optional<PendingPersonnel> findByEmail(String email);

    /**
     * Vérifie si un personnel en attente existe avec ce numéro de téléphone.
     *
     * @param telephone Le numéro de téléphone
     * @return true si un personnel avec ce téléphone existe, sinon false
     */
    boolean existsByTelephone(String telephone);

    /**
     * Vérifie si un personnel en attente existe avec cette adresse email.
     *
     * @param email L'adresse email
     * @return true si un personnel avec cet email existe, sinon false
     */
    boolean existsByEmail(String email);
}
