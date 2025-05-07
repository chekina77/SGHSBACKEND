
        package com.example.SGHS4.service;

import com.example.SGHS4.dto.*;
import com.example.SGHS4.entite.AppointementDoctor;
import com.example.SGHS4.entite.Doctor;
import com.example.SGHS4.entite.Patient;
import com.example.SGHS4.repository.AppointementDoctorRepository;
import com.example.SGHS4.repository.DoctorRepository;
import com.example.SGHS4.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppointementDoctorService {

    @Autowired
    private AppointementDoctorRepository appointementDoctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    /**
     * Crée un patient et un rendez-vous, puis retourne un DTO de réponse.
     */
    public AppointmentResponseDTO createAppointment(CreateAppointmentDTO dto) {
        Doctor doc = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Médecin introuvable"));

        Patient p = new Patient();
        p.setName(dto.getName());
        p.setSurname(dto.getSurname());
        p.setSexe(dto.getSexe());
        p.setDateOfBirth(dto.getDateOfBirth());
        p.setWeight(dto.getWeight());
        p.setHeight(dto.getHeight());
        p.setEmail(dto.getEmail());
        p.setNationalIDcardnumber(dto.getNationalIDcardnumber());
        p.setPhoneNumber(dto.getPhoneNumber());
        p.setAllergies(dto.getAllergies());
        p.setComment(dto.getComment());
        p.setDateOfToday(dto.getDateOfToday());
        p.setAssignedDoctor(doc);
        patientRepository.save(p);

        AppointementDoctor ap = new AppointementDoctor();
        ap.setPatient(p);
        ap.setDoctor(doc);
        ap.setDate(dto.getDateOfToday().atStartOfDay());
        ap.setStatus("alive");
        appointementDoctorRepository.save(ap);

        return convertToResponseDTO(ap);
    }

    /**
     * Récupère un rendez-vous par ID.
     */
    public AppointmentResponseDTO getAppointmentById(Long id) {
        AppointementDoctor ap = appointementDoctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rendez-vous non trouvé : " + id));
        return convertToResponseDTO(ap);
    }

    /**
     * Récupère tous les rendez-vous.
     */
    public List<AppointmentResponseDTO> getAllAppointments() {
        return appointementDoctorRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Recherche des rendez-vous par mot-clé (nom patient ou médecin).
     */
    public List<AppointmentResponseDTO> searchAppointments(String keyword) {
        List<AppointementDoctor> results = (List<AppointementDoctor>) appointementDoctorRepository;
        return results.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les rendez-vous d’un médecin pour une date donnée.
     */
    public List<AppointmentResponseDTO> getDoctorAppointmentsForDate(Long doctorId, String date) {
        LocalDate localDate = LocalDate.parse(date);
        LocalDateTime start = localDate.atStartOfDay();
        LocalDateTime end = localDate.atTime(LocalTime.MAX);
        List<AppointementDoctor> list = appointementDoctorRepository
                .findByDoctorIdAndDateBetween(doctorId, start, end);
        return list.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    /**
     * Supprime un rendez-vous.
     */
    public void deleteAppointment(Long appointmentId) {
        appointementDoctorRepository.deleteById(appointmentId);
    }

    /**
     * Liste tous les médecins (pour le front-end).
     */
    public List<Doctor> listDoctors() {
        return doctorRepository.findAll();
    }

    /**
     * Conversion interne vers DTO de réponse.
     */
    private AppointmentResponseDTO convertToResponseDTO(AppointementDoctor ap) {
        AppointmentResponseDTO dto = new AppointmentResponseDTO();
        dto.setId(ap.getId());
        dto.setDate(ap.getDate());
        dto.setStatus(ap.getStatus());

        // patient info
        PatientDTO pDto = new PatientDTO();
        pDto.setId(ap.getPatient().getId());
        pDto.setNom(ap.getPatient().getName());
        pDto.setPrenom(ap.getPatient().getSurname());
        pDto.setDateNaissance(ap.getPatient().getDateOfBirth());
        pDto.setSexe(ap.getPatient().getSexe());
        pDto.setTelephone(ap.getPatient().getPhoneNumber());
        pDto.setEmail(ap.getPatient().getEmail());
        pDto.setAllergies(ap.getPatient().getAllergies());
        pDto.setAntecedentsMedicaux(ap.getPatient().getComment());
        pDto.setDateInscription(ap.getPatient().getDateOfToday());
        dto.setPatient(pDto);

        // doctor info
        DoctorDTO dDto = new DoctorDTO();
        dDto.setId(ap.getDoctor().getId());
        dDto.setNom(ap.getDoctor().getNom());
        dDto.setPrenom(ap.getDoctor().getPrenom());
        dDto.setEmail(ap.getDoctor().getEmail());
        dDto.setTelephone(ap.getDoctor().getTelephone());
        dto.setDoctor(dDto);

        return dto;
    }
}
