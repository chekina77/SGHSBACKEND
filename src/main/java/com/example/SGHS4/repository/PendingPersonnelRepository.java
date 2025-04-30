package com.example.SGHS4.repository;

import com.example.SGHS4.entite.PendingPersonnel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour l'entité PendingPersonnel.
 */
@Repository
public interface PendingPersonnelRepository extends JpaRepository<PendingPersonnel, Long> {
    /**
     * Recherche un PendingPersonnel par son numéro de CNI.
     *
     * @param cni le numéro de CNI
     * @return un Optional contenant le PendingPersonnel s'il existe
     */
    Optional<PendingPersonnel> findByCni(String cni);

    /**
     * Vérifie l'existence d'un PendingPersonnel avec le CNI donné.
     *
     * @param cni le numéro de CNI
     * @return true si un enregistrement existe, false sinon
     */
    boolean existsByCni(String cni);

    /**
     * (Optionnel) Recherche par email
     */
    Optional<PendingPersonnel> findByEmail(String email);

    /**
     * (Optionnel) Vérifie existence par téléphone
     */
    boolean existsByTelephone(String telephone);

    /**
     * (Optionnel) Vérifie existence par email
     */
    boolean existsByEmail(String email);
}
