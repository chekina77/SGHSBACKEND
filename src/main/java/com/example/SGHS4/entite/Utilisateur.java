package com.example.SGHS4.entite;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import com.example.SGHS4.enums.TypeDeRole;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "utilisateur")
public class Utilisateur implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mot_de_passe")
    private String mdp;

    private String nom;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String telephone;

    @Column(unique = true)
    private String cni;  //  Ajout du champ CNI

    @Column(name = "verification_code")  // Champ pour le code de vérification
    private String verificationCode;  // Code de vérification pour l'utilisateur

    private boolean actif = false;

    @Enumerated(EnumType.STRING)
    private TypeDeRole role;

    public Utilisateur() {}

    public Utilisateur(Long id, String mdp, String nom, String email, String telephone, String cni, String verificationCode, boolean actif, TypeDeRole role) {
        this.id = id;
        this.mdp = mdp;
        this.nom = nom;
        this.email = email;
        this.telephone = telephone;
        this.cni = cni;
        this.verificationCode = verificationCode;  // Initialisation du code de vérification
        this.actif = actif;
        this.role = role;
    }

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMdp() { return mdp; }
    public void setMdp(String mdp) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        this.mdp = encoder.encode(mdp);
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getCni() { return cni; }  // Getter pour CNI
    public void setCni(String cni) { this.cni = cni; }  // Setter pour CNI

    public String getVerificationCode() { return verificationCode; }  // Getter pour le code de vérification
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }  // Setter pour le code de vérification

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public TypeDeRole getRole() { return role; }
    public void setRole(TypeDeRole role) { this.role = role; }

    // Implémentation UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + this.role.name())
        );
    }

    @Override
    public String getPassword() { return this.mdp; }

    @Override
    public String getUsername() { return this.email; }

    @Override
    public boolean isAccountNonExpired() { return this.actif; }

    @Override
    public boolean isAccountNonLocked() { return this.actif; }

    @Override
    public boolean isCredentialsNonExpired() { return this.actif; }

    @Override
    public boolean isEnabled() { return this.actif; }
}
