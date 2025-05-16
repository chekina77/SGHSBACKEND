package com.example.SGHS4.controller;

import com.example.SGHS4.dto.LivretDTO;
import com.example.SGHS4.entite.Livret;
import com.example.SGHS4.service.LivretService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/livret")
public class LivretController {

    @Autowired
    private LivretService livretService;

    @PostMapping("/create")
    public ResponseEntity<Livret> createOrUpdateLivret(@RequestBody LivretDTO livretDTO) {
        try {
            Livret livret = livretService.createOrUpdateLivret(livretDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(livret);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
