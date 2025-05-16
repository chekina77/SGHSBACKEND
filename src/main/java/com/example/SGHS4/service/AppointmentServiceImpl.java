package com.example.SGHS4.service;

import com.example.SGHS4.dto.AppointmentDTO;
import com.example.SGHS4.dto.AppointmentResponseDTO;
import com.example.SGHS4.entite.AppointementDoctor;
import com.example.SGHS4.repository.AppointementDoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointementDoctorRepository appointementDoctorRepository;

    @Override
    public List<AppointmentDTO> getAllAppointments() {
        return appointementDoctorRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentDTO> getRendezvous() {
        return appointementDoctorRepository.findAll().stream()
                .filter(a -> a.getPatientName() != null)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentResponseDTO> searchAppointments(String keyword) {
        return appointementDoctorRepository
                .findByPatientNameContainingIgnoreCaseOrMedecinNameContainingIgnoreCase(keyword, keyword).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void updateAppointmentStatut(Long id, String statut) {
        AppointementDoctor appointment = appointementDoctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rendez-vous non trouvé avec ID : " + id));

        appointment.setStatut(statut);
        appointementDoctorRepository.save(appointment);
    }

    @Override
    public void deleteAppointment(Long id) {
        if (appointementDoctorRepository.existsById(id)) {
            appointementDoctorRepository.deleteById(id);
        } else {
            throw new RuntimeException("Rendez-vous non trouvé avec l'ID : " + id);
        }
    }

    // Méthodes utilitaires
    private AppointmentDTO mapToDTO(AppointementDoctor a) {
        return new AppointmentDTO(
                a.getId(),
                a.getPatientName(),
                a.getMedecinName(),
                a.getStatut(),
                a.getAppointmentDate()
        );
    }

    private AppointmentResponseDTO convertToResponseDTO(AppointementDoctor a) {
        return new AppointmentResponseDTO(
                a.getId(),
                a.getPatientName(),
                a.getAppointmentDate(),
                a.getMedecinName(),
                a.getStatut()
        );
    }

    @Override
    public List<AppointmentDTO> searchDoctors(String keyword) {
        return null; // À implémenter si tu veux rechercher par nom de docteur spécifiquement
    }
}
