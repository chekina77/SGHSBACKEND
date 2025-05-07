package com.example.SGHS4.service;

import com.example.SGHS4.entite.Appointment;
import com.example.SGHS4.repository.AppointementRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminAppointementService {

    private final AppointementRepository appointementRepository;

    public AdminAppointementService(AppointementRepository appointementRepository) {
        this.appointementRepository = appointementRepository;
    }

    public List<Appointment> getAllAppointments() {
        return appointementRepository.findAllByOrderByAppointmentDateDesc();
    }

    public Appointment save(Appointment appointement) {
        return appointementRepository.save(appointement);
    }
    public List<Appointment> searchAppointments(String keyword) {
        return appointementRepository.searchByKeyword(keyword);
    }

}

