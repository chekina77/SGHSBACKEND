package com.example.SGHS4.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.example.SGHS4.entite.Jwt;

import java.util.Optional;
import java.util.stream.Stream;


public interface JwtRepository extends CrudRepository<Jwt, Integer> {
    Optional<Jwt> findByValueAndDesactiveAndExpire(String value, boolean desactive, boolean expire);

    @Query("FROM Jwt j WHERE j.expire = :expire AND j.desactive = :desactive AND j.utilisateur.email = :email")
    Optional<Jwt> findUtilisateurValidToken(String email, boolean desactive, boolean expire);

    @Query("FROM Jwt j WHERE j.utilisateur.email = :email")
    Stream<Jwt> findUtilisateur(String email);

    @Query("FROM Jwt j WHERE j.refreshToken.valeur = :valeur")
    Optional<Jwt> findByRefreshToken(String valeur);

    void deleteAllByExpireAndDesactive(boolean expire, boolean desactive);
}