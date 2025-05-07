package com.example.SGHS4.repository;

import com.example.SGHS4.entite.CodeReinitialisation;
import com.example.SGHS4.entite.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodeReinitialisationRepository extends JpaRepository<CodeReinitialisation, Long> {
    Optional<CodeReinitialisation> findByUtilisateur(Utilisateur utilisateur);
    List<CodeReinitialisation> findByUtilisateurAndActifTrue(Utilisateur utilisateur);


}


