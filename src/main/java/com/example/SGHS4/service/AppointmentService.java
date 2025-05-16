package com.example.SGHS4.service;

import com.example.SGHS4.dto.AppointmentDTO;
import com.example.SGHS4.dto.AppointmentResponseDTO;

import java.util.List;

public interface AppointmentService {


    List<AppointmentDTO> getAllAppointments();


    void deleteAppointment(Long id);

    List<AppointmentDTO> getRendezvous();

    List<AppointmentResponseDTO> searchAppointments(String keyword);

    List<AppointmentDTO> searchDoctors(String keyword);

    void updateAppointmentStatut(Long id, String statut);
}
