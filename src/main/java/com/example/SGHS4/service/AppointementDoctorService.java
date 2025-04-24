package com.example.SGHS4.service;

import com.example.SGHS4.entite.AppointementDoctor;
import com.example.SGHS4.repository.AppointementDoctorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AppointementDoctorService {

    private final AppointementDoctorRepository appointementDoctorRepository;

    public AppointementDoctorService(AppointementDoctorRepository appointementDoctorRepository) {
        this.appointementDoctorRepository = appointementDoctorRepository;
    }

    public List<AppointementDoctor> getAllAppointments() {
        return appointementDoctorRepository.findAll();
    }

    public AppointementDoctor createAppointment(AppointementDoctor appointment) {
        return appointementDoctorRepository.save(appointment);
    }

    public Optional<AppointementDoctor> getAppointmentById(Long id) {
        return appointementDoctorRepository.findById(id);
    }

    public Optional<AppointementDoctor> updateAppointment(Long id, AppointementDoctor appointmentDetails) {
        return appointementDoctorRepository.findById(id).map(existing -> {
            existing.setName(appointmentDetails.getName());
            existing.setDate(appointmentDetails.getDate());
            return appointementDoctorRepository.save(existing);
        });
    }


    public List<AppointementDoctor> searchAppointments(String keyword) {
        return appointementDoctorRepository.findByNameContainingIgnoreCase(keyword);
    }
}
