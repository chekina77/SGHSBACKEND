package com.example.SGHS4.entite;

import com.example.SGHS4.TypeDeRole;
import jakarta.persistence.*;

@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Enumerated(EnumType.STRING)

    private TypeDeRole libelle;


    public Role() {
    }

    public Role(int id, TypeDeRole libelle) {
        this.id = id;
        this.libelle = libelle;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TypeDeRole getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle =  TypeDeRole.valueOf(libelle);
    }
}
