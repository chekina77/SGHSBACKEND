package com.example.SGHS4.repository;

import com.example.SGHS4.entite.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.SGHS4.entite.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


@Repository
public interface JwtRepository extends JpaRepository<Jwt, Long> {
    void deleteByUtilisateurId(Long utilisateurId);


    /**
     * Trouve un JWT par sa valeur et ses états (désactivé et expiré)
     * @param value La valeur du token
     * @param desactive Si le token est désactivé
     * @param expire Si le token est expiré
     * @return Le token correspondant, s'il existe
     */
    Optional<Jwt> findByValueAndDesactiveAndExpire(String value, boolean desactive, boolean expire);

    /**
     * Trouve un token valide pour un utilisateur donné
     * @param email L'email de l'utilisateur
     * @param desactive Si le token est désactivé
     * @param expire Si le token est expiré
     * @return Le token valide, s'il existe
     */
    @Query("FROM Jwt j WHERE j.expire = :expire AND j.desactive = :desactive AND j.utilisateur.email = :email")
    Optional<Jwt> findUtilisateurValidToken(
            @Param("email") String email,
            @Param("desactive") boolean desactive,
            @Param("expire") boolean expire
    );

    /**
     * Trouve tous les tokens d'un utilisateur
     * @param email L'email de l'utilisateur
     * @return Un stream de tokens
     */
    @Query("FROM Jwt j WHERE j.utilisateur.email = :email")
    Stream<Jwt> findUtilisateur(@Param("email") String email);

    /**
     * Trouve tous les tokens d'un utilisateur sous forme de liste
     * @param email L'email de l'utilisateur
     * @return Une liste de tokens
     */
    @Query("FROM Jwt j WHERE j.utilisateur.email = :email")
    List<Jwt> findAllByUtilisateurEmail(@Param("email") String email);

    /**
     * Trouve un JWT par la valeur de son refresh token
     * @param valeur La valeur du refresh token
     * @return Le JWT correspondant, s'il existe
     */
    @Query("FROM Jwt j WHERE j.refreshToken.valeur = :valeur")
    Optional<Jwt> findByRefreshToken(@Param("valeur") String valeur);

    /**
     * Supprime tous les tokens qui sont à la fois expirés et désactivés
     * @param expire Si le token est expiré
     * @param desactive Si le token est désactivé
     * @return Le nombre de tokens supprimés
     */
    @Modifying
    @Query("DELETE FROM Jwt j WHERE j.expire = :expire AND j.desactive = :desactive")
    long deleteAllByExpireAndDesactive(
            @Param("expire") boolean expire,
            @Param("desactive") boolean desactive
    );

    /**
     * Trouve tous les tokens expirés avant une date donnée
     * Utile pour nettoyer les anciens tokens
     * @param date La date limite d'expiration
     * @return Une liste de tokens expirés
     */
    @Query("FROM Jwt j WHERE j.refreshToken.expiration < :date")
    List<Jwt> findAllWithRefreshTokenExpiredBefore(@Param("date") Instant date);

    /**
     * Marque comme expirés et désactivés tous les tokens d'un utilisateur
     * @param email L'email de l'utilisateur
     * @return Le nombre de tokens mis à jour
     */
    @Modifying
    @Query("UPDATE Jwt j SET j.expire = true, j.desactive = true WHERE j.utilisateur.email = :email")
    int invalidateAllTokensForUser(@Param("email") String email);
    List<Jwt> findByUtilisateur(Utilisateur utilisateur);

}