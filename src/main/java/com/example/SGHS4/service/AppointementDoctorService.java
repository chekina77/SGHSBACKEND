
package com.example.SGHS4.service;

import com.example.SGHS4.dto.*;
import com.example.SGHS4.entite.AppointementDoctor;
import com.example.SGHS4.entite.Doctor;
import com.example.SGHS4.entite.Patient;
import com.example.SGHS4.entite.Utilisateur;
import com.example.SGHS4.repository.AppointementDoctorRepository;
import com.example.SGHS4.repository.DoctorRepository;
import com.example.SGHS4.repository.UtilisateurRepository;
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
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    /**
     * Crée un patient et un rendez-vous, puis retourne un DTO de réponse.
     */
    public AppointmentResponseDTO createAppointmentUsingUtilisateur(CreateAppointmentDTO dto) {
        // 1. Enregistrer le patient
        Patient patient = new Patient();
        patient.setName(dto.getName());
        patient.setSurname(dto.getSurname());
        patient.setSexe(dto.getSexe());
        patient.setDateOfBirth(dto.getDateOfBirth());
        patient.setWeight(dto.getWeight());
        patient.setHeight(dto.getHeight());
        patient.setEmail(dto.getEmail());
        patient.setNationalIDcardnumber(dto.getNationalIDcardnumber());
        patient.setPhoneNumber(dto.getPhoneNumber());
        patient.setAllergies(dto.getAllergies());
        patient.setComment(dto.getComment());
        patient.setDateOfToday(dto.getDateOfToday());

        // 2. Récupérer le médecin depuis la table Utilisateur
        Utilisateur medecin = utilisateurRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Utilisateur (médecin) non trouvé avec ID : " + dto.getDoctorId()));

        // 3. Enregistrer le patient
        Patient savedPatient = patientRepository.save(patient);

        // 4. Créer le rendez-vous
        AppointementDoctor appointment = new AppointementDoctor();
        appointment.setPatientName(savedPatient.getName());
        appointment.setAppointmentDate(savedPatient.getDateOfToday().atStartOfDay());
        appointment.setMedecinName(medecin.getNom());
        appointment.setUtilisateur(medecin); // Attention : il faut que ton entité AppointementDoctor ait un champ Utilisateur
        appointment.setStatut("En attente"); // ou un autre statut par défaut


        AppointementDoctor savedAppointment = appointementDoctorRepository.save(appointment);

        // 5. Mapper en DTO de réponse
        AppointmentResponseDTO response = new AppointmentResponseDTO();
        response.setId(savedAppointment.getId());
        response.setPatientName(savedAppointment.getPatientName());
        response.setAppointmentDate(savedAppointment.getAppointmentDate());  // Utilisation de LocalDateTime
        response.setMedecinName(savedAppointment.getMedecinName());

        return response;
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
        List<AppointementDoctor> results = appointementDoctorRepository
                .findByPatientNameContainingIgnoreCaseOrMedecinNameContainingIgnoreCase(keyword, keyword);
        return results.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
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
        dto.setPatientName(ap.getPatientName());

        // ✅ conversion de LocalDateTime vers LocalDate
        dto.setAppointmentDate(ap.getAppointmentDate()); // Utilisation du LocalDateTime directement

        dto.setMedecinName(ap.getMedecinName());
        dto.setStatut(ap.getStatut());  // Assure-toi que ce champ existe dans le DTO

        return dto;
    }

    /*public List<AppointmentResponseDTO> getDoctorAppointmentsForDate(Long doctorId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // 🔁 Récupération de l'objet Doctor à partir de son ID
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé avec ID : " + doctorId));

        // ✅ Appel correct avec l'objet doctor
        List<AppointementDoctor> appointments = appointementDoctorRepository
                .findByUtilisateurAndAppointmentDateBetween(doctor, startOfDay, endOfDay);

        return appointments.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }*/

}
