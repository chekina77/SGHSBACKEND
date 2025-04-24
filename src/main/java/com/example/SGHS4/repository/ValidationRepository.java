package com.example.SGHS4.repository;

import com.example.SGHS4.entite.Validation;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ValidationRepository extends CrudRepository<Validation, Integer> {
    Optional<Validation> findByCode(String code);
}
