package com.example.SGHS4.service;

import com.example.SGHS4.dto.*;

import java.util.List;

public interface AppointmentService {

    AppointmentResponseDTO createAppointmentUsingUtilisateur(CreateAppointmentDTO dto, String username);

    AppointmentResponseDTO getAppointmentById(Long id);

    List<AppointmentDTO> getAllAppointments();

    List<AppointmentDTO> getRendezvous();

    List<AppointmentResponseDTO> searchAppointments(String keyword);

    void updateAppointmentStatut(Long id, String statut);

    void deleteAppointment(Long id);
    List<PatientDTO> getAllPatients();  // récupère tous les patients avec nom, prénom, email, téléphone





    /**
     * Récupère la liste des patients en déchiffrant leurs données.
     * @param cni CNI en clair fournie une seule fois (clé personnelle)
     * @return liste des patients dont les données sont déchiffrées avec la clé CNI
     */
    PatientDTO getPatientDecryptedInfo(Long patientId);
}
