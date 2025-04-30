package com.example.SGHS4.service;

import com.example.SGHS4.enums.TypeDeRole;
import com.example.SGHS4.entite.Role;
import com.example.SGHS4.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class InitialisationService {

    private final RoleRepository roleRepository;

    public InitialisationService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void initRoles() {
        Arrays.stream(TypeDeRole.values()).forEach(typeDeRole -> {
            roleRepository.findByLibelle(typeDeRole).orElseGet(() -> {
                Role role = new Role();
                role.setLibelle(typeDeRole);
                return roleRepository.save(role);
            });
        });
        System.out.println("Rôles initialisés avec succès.");
    }
}
