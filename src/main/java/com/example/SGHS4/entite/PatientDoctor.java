package com.example.SGHS4.entite;

import jakarta.persistence.*;

@Entity
@Table (name = "PatientDoctors")
public class PatientDoctor {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id ;
    private String name;
    private int age;
    private String phoneNumber;
    private String lastvisit;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getLastvisit() {
        return lastvisit;
    }

    public void setLastvisit(String lastvisit) {
        this.lastvisit = lastvisit;
    }
}
