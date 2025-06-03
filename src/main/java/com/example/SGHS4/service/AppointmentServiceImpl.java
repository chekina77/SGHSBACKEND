package com.example.SGHS4.service;

import com.example.SGHS4.dto.AppointmentDTO;
import com.example.SGHS4.dto.AppointmentResponseDTO;
import com.example.SGHS4.dto.CreateAppointmentDTO;
import com.example.SGHS4.dto.PatientDTO;
import com.example.SGHS4.entite.*;
import com.example.SGHS4.repository.*;
import com.example.SGHS4.util.AESUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointementDoctorRepository appointementDoctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Override
    public AppointmentResponseDTO createAppointmentUsingUtilisateur(CreateAppointmentDTO dto, String username) {
        Utilisateur medecinConnecte = utilisateurRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur connecté non trouvé"));

        Utilisateur medecinCible = utilisateurRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé"));

        String cni = dto.getNationalIDcardnumber();
        Patient patient = new Patient();

        try {
            // Chiffrement de la CNI avec une clé serveur (fixe)
            String encryptedCNI = AESUtil.encrypt(cni, "master-key-very-secret");

            // Chiffrement des données sensibles avec la CNI comme clé
            patient.setName(AESUtil.encrypt(dto.getName(), cni));
            patient.setSurname(AESUtil.encrypt(dto.getSurname(), cni));
            patient.setSexe(AESUtil.encrypt(dto.getSexe(), cni));
            patient.setDateOfBirth(dto.getDateOfBirth());
            patient.setWeight(dto.getWeight());
            patient.setHeight(dto.getHeight());
            patient.setEmail(AESUtil.encrypt(dto.getEmail(), cni));
            patient.setPhoneNumber(AESUtil.encrypt(dto.getPhoneNumber(), cni));
            patient.setAllergies(AESUtil.encrypt(dto.getAllergies(), cni));
            patient.setComment(AESUtil.encrypt(dto.getComment(), cni));
            patient.setDateOfToday(dto.getDateOfToday());

            // Stocker la CNI chiffrée avec la master key
            patient.setEncryptedCNI(encryptedCNI);
            patient.setMedecinName(medecinCible.getNom());

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du chiffrement des données du patient : " + e.getMessage());
        }

        patientRepository.save(patient);

        AppointementDoctor appointment = new AppointementDoctor();
        appointment.setPatientName(dto.getName() + " " + dto.getSurname());
        appointment.setMedecinName(medecinCible.getNom());
        appointment.setAppointmentDate(LocalDateTime.now());
        appointment.setStatut("En attente");
        appointment.setUtilisateur(medecinConnecte);

        AppointementDoctor savedAppointment = appointementDoctorRepository.save(appointment);

        AppointmentResponseDTO responseDTO = new AppointmentResponseDTO();
        responseDTO.setId(savedAppointment.getId());
        responseDTO.setPatientName(savedAppointment.getPatientName());
        responseDTO.setMedecinName(savedAppointment.getMedecinName());
        responseDTO.setAppointmentDate(savedAppointment.getAppointmentDate());
        responseDTO.setStatut(savedAppointment.getStatut());

        return responseDTO;
    }

    public PatientDTO getPatientDecryptedInfo(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient non trouvé"));

        try {
            if (patient.getEncryptedCNI() == null || patient.getEncryptedCNI().isEmpty()) {
                throw new RuntimeException("CNI chiffrée manquante !");
            }

            // Déchiffrement de la CNI avec la masterKey
            String decryptedCNI = AESUtil.decryptCNI(patient.getEncryptedCNI());

            if (decryptedCNI == null || decryptedCNI.isEmpty()) {
                throw new RuntimeException("La CNI déchiffrée est nulle ou vide");
            }

            // Déchiffrement des autres données sensibles avec la CNI déchiffrée comme clé
            String name = AESUtil.decrypt(patient.getName(), decryptedCNI);
            String surname = AESUtil.decrypt(patient.getSurname(), decryptedCNI);
            String sexe = AESUtil.decrypt(patient.getSexe(), decryptedCNI);
            String email = AESUtil.decrypt(patient.getEmail(), decryptedCNI);
            String phoneNumber = AESUtil.decrypt(patient.getPhoneNumber(), decryptedCNI);
            String allergies = AESUtil.decrypt(patient.getAllergies(), decryptedCNI);
            String comment = AESUtil.decrypt(patient.getComment(), decryptedCNI);

            // Préparer le DTO
            PatientDTO dto = new PatientDTO();
            dto.setName(name);
            dto.setSurname(surname);
            dto.setSexe(sexe);
            dto.setDateOfBirth(patient.getDateOfBirth());
            dto.setWeight(patient.getWeight());
            dto.setHeight(patient.getHeight());
            dto.setEmail(email);
            dto.setPhoneNumber(phoneNumber);
            dto.setAllergies(allergies);
            dto.setComment(comment);
            dto.setDateOfToday(patient.getDateOfToday());

            return dto;

        } catch (Exception e) {
            throw new RuntimeException("Erreur de déchiffrement des données : " + e.getMessage(), e);
        }
    }


    @Override
    public void deleteAppointment(Long id) {
        appointmentRepository.deleteById(id);
    }

    @Override
    public AppointmentResponseDTO getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .map(appointment -> new AppointmentResponseDTO(
                        appointment.getId(),
                        appointment.getPatient().getName() + " " + appointment.getPatient().getSurname(),
                        appointment.getAppointmentDate(),
                        appointment.getDoctor().getNom(),
                        appointment.getStatus()
                ))
                .orElseThrow(() -> new RuntimeException("Rendez-vous non trouvé avec id " + id));
    }

    @Override
    public List<AppointmentDTO> getAllAppointments() {
        return appointementDoctorRepository.findAll().stream()
                .map(appointement -> new AppointmentDTO(
                        appointement.getId(),
                        appointement.getPatientName(),
                        appointement.getMedecinName(),
                        appointement.getStatut(),
                        appointement.getAppointmentDate()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentDTO> getRendezvous() {
        return getAllAppointments();
    }

    @Override
    public List<AppointmentResponseDTO> searchAppointments(String keyword) {
        return appointmentRepository.findAll().stream()
                .filter(appointment ->
                        appointment.getPatient() != null &&
                                appointment.getPatient().getName() != null &&
                                appointment.getPatient().getName().toLowerCase().contains(keyword.toLowerCase())
                )
                .map(appointment -> new AppointmentResponseDTO(
                        appointment.getId(),
                        appointment.getPatient().getName() + " " + appointment.getPatient().getSurname(),
                        appointment.getAppointmentDate(),
                        appointment.getDoctor().getNom(),
                        appointment.getStatus()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public void updateAppointmentStatut(Long id, String statut) {
        appointmentRepository.findById(id).ifPresent(appointment -> {
            appointment.setStatus(statut);
            appointmentRepository.save(appointment);
        });
    }
    @Override
    public List<PatientDTO> getAllPatients() {
        List<Patient> patients = patientRepository.findAll();
        return patients.stream()
                .map(patient -> getPatientDecryptedInfo(patient.getId()))
                .collect(Collectors.toList());
    }



}
