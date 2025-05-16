package com.example.SGHS4.repository;

import com.example.SGHS4.entite.Utilisateur;
import com.example.SGHS4.enums.TypeDeRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    List<Utilisateur> findByRoleIn(List<TypeDeRole> roles);

    Optional<Utilisateur> findByEmail(String email);
    Optional<Utilisateur> findByCni(String cni);

    boolean existsByCni(String cni);
    boolean existsByTelephone(String telephone);
    boolean existsByEmail(String email);

    List<Utilisateur> findByRole(TypeDeRole role);

    // ✅ Nouvelle méthode ajoutée
    Optional<Utilisateur> findByIdAndRole(Long id, TypeDeRole role);
    void deleteById(Long id);
    Optional<Utilisateur> findById(Long id); // Méthode pour trouver un utilisateur par son ID
    boolean existsById(Long id);
    @Query("SELECT u FROM Utilisateur u WHERE u.role IN :roles AND (LOWER(u.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Utilisateur> searchByRoleAndKeyword(@Param("roles") List<TypeDeRole> roles, @Param("keyword") String keyword);



}

