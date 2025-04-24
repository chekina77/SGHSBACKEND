package com.example.SGHS4.service;

import com.example.SGHS4.entite.Appointement;
import com.example.SGHS4.repository.AppointementRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminAppointementService {

    private final AppointementRepository appointementRepository;

    public AdminAppointementService(AppointementRepository appointementRepository) {
        this.appointementRepository = appointementRepository;
    }

    public List<Appointement> getAllAppointments() {
        return appointementRepository.findAllByOrderByDateDesc();
    }

    public Appointement save(Appointement appointement) {
        return appointementRepository.save(appointement);
    }
    public List<Appointement> searchAppointments(String keyword) {
        return appointementRepository.searchByKeyword(keyword);
    }

}

