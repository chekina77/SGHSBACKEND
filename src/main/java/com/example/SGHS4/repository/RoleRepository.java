package com.example.SGHS4.repository;

import com.example.SGHS4.enums.TypeDeRole;
import com.example.SGHS4.entite.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByLibelle(TypeDeRole libelle);
}
