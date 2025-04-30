package com.example.SGHS4.service;

import com.example.SGHS4.dto.AppointmentResponseDTO;
import com.example.SGHS4.dto.CreateAppointmentDTO;
import com.example.SGHS4.entite.AppointementDoctor;
import com.example.SGHS4.entite.Doctor;
import com.example.SGHS4.entite.Patient;
import com.example.SGHS4.exceptions.ResourceNotFoundException;
import com.example.SGHS4.exceptions.ValidationException;
import com.example.SGHS4.repository.AppointementDoctorRepository;
import com.example.SGHS4.repository.DoctorRepository;
import com.example.SGHS4.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AppointementDoctorService {

    private static final Logger logger = LoggerFactory.getLogger(AppointementDoctorService.class);

    private final AppointementDoctorRepository appointmentRepo;
    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;

    @Autowired
    public AppointementDoctorService(
            AppointementDoctorRepository appointmentRepo,
            PatientRepository patientRepo,
            DoctorRepository doctorRepo) {
        this.appointmentRepo = appointmentRepo;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
    }

    /**
     * Crée un nouveau rendez-vous avec un médecin
     * @param dto Les données du rendez-vous et du patient
     * @return Le rendez-vous créé
     * @throws ValidationException Si les données sont invalides
     * @throws ResourceNotFoundException Si le médecin n'existe pas
     */
    @Transactional
    public AppointmentResponseDTO createAppointment(CreateAppointmentDTO dto) {
        logger.info("Création d'un nouveau rendez-vous médical");

        // Validation des données
        validateAppointmentData(dto);

        // Vérifier si le médecin existe
        Doctor doctor = checkDoctorExists(dto.getDoctorId());

        // Chercher si le patient existe déjà par numéro de carte d'identité
        Patient patient = findOrCreatePatient(dto);

        // Création du rendez-vous
        AppointementDoctor appointment = new AppointementDoctor();
        appointment.setPatient(patient);

        // Analyser la date et l'heure du rendez-vous
        LocalDateTime appointmentDateTime = parseAppointmentDateTime(dto.getDate());
        appointment.setDate(appointmentDateTime);
        appointment.setDoctorId(dto.getDoctorId());
        appointment.setStatus("PROGRAMMÉ"); // Statut par défaut
        appointment.setCreatedAt(LocalDateTime.now());

        // Vérifier les conflits de rendez-vous
        checkAppointmentConflicts(doctor, appointmentDateTime);

        // Sauvegarder le rendez-vous
        AppointementDoctor savedAppointment = appointmentRepo.save(appointment);
        logger.info("Rendez-vous créé avec succès, ID: {}", savedAppointment.getId());

        // Convertir en DTO de réponse
        return convertToResponseDTO(savedAppointment, doctor);
    }

    /**
     * Récupère un rendez-vous par son ID
     * @param appointmentId L'ID du rendez-vous
     * @return Les détails du rendez-vous
     * @throws ResourceNotFoundException Si le rendez-vous n'existe pas
     */
    @Transactional(readOnly = true)
    public AppointmentResponseDTO getAppointmentById(Long appointmentId) {
        logger.info("Récupération du rendez-vous ID: {}", appointmentId);

        AppointementDoctor appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> {
                    logger.warn("Rendez-vous ID {} non trouvé", appointmentId);
                    return new ResourceNotFoundException("Rendez-vous non trouvé avec l'ID: " + appointmentId);
                });

        Doctor doctor = checkDoctorExists(appointment.getDoctorId());

        return convertToResponseDTO(appointment, doctor);
    }

    /**
     * Récupère tous les rendez-vous d'un médecin pour une date donnée
     * @param doctorId L'ID du médecin
     * @param date La date (au format yyyy-MM-dd)
     * @return La liste des rendez-vous
     * @throws ResourceNotFoundException Si le médecin n'existe pas
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getDoctorAppointmentsForDate(Long doctorId, String dateStr) {
        logger.info("Récupération des rendez-vous du médecin {} pour la date {}", doctorId, dateStr);

        // Vérifier si le médecin existe
        Doctor doctor = checkDoctorExists(doctorId);

        // Convertir la chaîne de date en LocalDate
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
        } catch (DateTimeParseException e) {
            logger.warn("Format de date invalide: {}", dateStr);
            throw new ValidationException("Format de date invalide. Utilisez le format yyyy-MM-dd");
        }

        // Début et fin de la journée
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // Récupérer les rendez-vous
        List<AppointementDoctor> appointments = appointmentRepo.findByDoctorIdAndDateBetween(
                doctorId, startOfDay, endOfDay);

        logger.info("{} rendez-vous trouvés pour le médecin {} à la date {}",
                appointments.size(), doctorId, dateStr);

        // Convertir en DTOs de réponse
        return appointments.stream()
                .map(appointment -> convertToResponseDTO(appointment, doctor))
                .collect(Collectors.toList());
    }

    /**
     * Met à jour le statut d'un rendez-vous
     * @param appointmentId L'ID du rendez-vous
     * @param status Le nouveau statut
     * @return Le rendez-vous mis à jour
     * @throws ResourceNotFoundException Si le rendez-vous n'existe pas
     * @throws ValidationException Si le statut est invalide
     */
    @Transactional
    public AppointmentResponseDTO updateAppointmentStatus(Long appointmentId, String status) {
        logger.info("Mise à jour du statut du rendez-vous ID {} vers {}", appointmentId, status);

        // Validation du statut
        if (status == null || status.trim().isEmpty()) {
            throw new ValidationException("Le statut ne peut pas être vide");
        }

        // Statuts autorisés
        List<String> validStatuses = List.of("PROGRAMMÉ", "CONFIRMÉ", "ANNULÉ", "TERMINÉ", "ABSENT");
        if (!validStatuses.contains(status.toUpperCase())) {
            throw new ValidationException("Statut invalide. Les valeurs autorisées sont: " + String.join(", ", validStatuses));
        }

        // Récupérer le rendez-vous
        AppointementDoctor appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> {
                    logger.warn("Rendez-vous ID {} non trouvé pour mise à jour", appointmentId);
                    return new ResourceNotFoundException("Rendez-vous non trouvé avec l'ID: " + appointmentId);
                });

        // Mettre à jour le statut
        appointment.setStatus(status.toUpperCase());
        appointment.setUpdatedAt(LocalDateTime.now());

        // Sauvegarder les modifications
        AppointementDoctor updatedAppointment = appointmentRepo.save(appointment);
        logger.info("Statut du rendez-vous ID {} mis à jour avec succès", appointmentId);

        Doctor doctor = checkDoctorExists(updatedAppointment.getDoctorId());

        return convertToResponseDTO(updatedAppointment, doctor);
    }

    /**
     * Supprime un rendez-vous
     * @param appointmentId L'ID du rendez-vous
     * @throws ResourceNotFoundException Si le rendez-vous n'existe pas
     */
    @Transactional
    public void deleteAppointment(Long appointmentId) {
        logger.info("Suppression du rendez-vous ID: {}", appointmentId);

        // Vérifier si le rendez-vous existe
        if (!appointmentRepo.existsById(appointmentId)) {
            logger.warn("Rendez-vous ID {} non trouvé pour suppression", appointmentId);
            throw new ResourceNotFoundException("Rendez-vous non trouvé avec l'ID: " + appointmentId);
        }

        // Supprimer le rendez-vous
        appointmentRepo.deleteById(appointmentId);
        logger.info("Rendez-vous ID {} supprimé avec succès", appointmentId);
    }

    // ---------- Méthodes privées ----------

    /**
     * Valide les données du rendez-vous
     * @param dto Les données à valider
     * @throws ValidationException Si les données sont invalides
     */
    private void validateAppointmentData(CreateAppointmentDTO dto) {
        if (dto == null) {
            throw new ValidationException("Les données du rendez-vous ne peuvent pas être nulles");
        }

        if (dto.getDoctorId() == null) {
            throw new ValidationException("L'ID du médecin est obligatoire");
        }

        if (dto.getDate() == null || dto.getDate().trim().isEmpty()) {
            throw new ValidationException("La date du rendez-vous est obligatoire");
        }

        if (dto.getPatient() == null) {
            throw new ValidationException("Les informations du patient sont obligatoires");
        }

        // Validation des données du patient
        if (dto.getPatient().getName() == null || dto.getPatient().getName().trim().isEmpty()) {
            throw new ValidationException("Le nom du patient est obligatoire");
        }

        if (dto.getPatient().getSurname() == null || dto.getPatient().getSurname().trim().isEmpty()) {
            throw new ValidationException("Le prénom du patient est obligatoire");
        }

        if (dto.getPatient().getNationalIDcardnumber() == null || dto.getPatient().getNationalIDcardnumber().trim().isEmpty()) {
            throw new ValidationException("Le numéro de carte d'identité du patient est obligatoire");
        }

        // Validation du format de la date
        try {
            LocalDateTime.parse(dto.getDate(), DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException e) {
            try {
                // Essayer avec juste la date
                LocalDate.parse(dto.getDate(), DateTimeFormatter.ISO_DATE);
            } catch (DateTimeParseException ex) {
                throw new ValidationException("Format de date invalide. Utilisez le format ISO (yyyy-MM-ddTHH:mm:ss)");
            }
        }
    }

    /**
     * Vérifie si le médecin existe
     * @param doctorId L'ID du médecin
     * @return Le médecin trouvé
     * @throws ResourceNotFoundException Si le médecin n'existe pas
     */
    private Doctor checkDoctorExists(Long doctorId) {
        return doctorRepo.findById(doctorId)
                .orElseThrow(() -> {
                    logger.warn("Médecin ID {} non trouvé", doctorId);
                    return new ResourceNotFoundException("Médecin non trouvé avec l'ID: " + doctorId);
                });
    }

    /**
     * Recherche un patient existant ou en crée un nouveau
     * @param dto Les données du patient
     * @return Le patient trouvé ou créé
     */
    private Patient findOrCreatePatient(CreateAppointmentDTO dto) {
        String nationalIdCard = dto.getPatient().getNationalIDcardnumber();
        Optional<Patient> existingPatient = patientRepo.findByNationalIDcardnumber(nationalIdCard);

        if (existingPatient.isPresent()) {
            logger.info("Patient existant trouvé avec le numéro de carte d'identité: {}", nationalIdCard);
            return existingPatient.get();
        }

        logger.info("Création d'un nouveau patient avec le numéro de carte d'identité: {}", nationalIdCard);

        // Création d'un nouveau patient
        Patient patient = new Patient();
        patient.setName(dto.getPatient().getName());
        patient.setSurname(dto.getPatient().getSurname());
        patient.setSexe(dto.getPatient().getSexe());

        // Conversion des dates
        try {
            if (dto.getPatient().getDateofbirth() != null && !dto.getPatient().getDateofbirth().isEmpty()) {
                patient.setDateofbirth(LocalDate.parse(dto.getPatient().getDateofbirth()));
            }

            if (dto.getPatient().getDateoftoday() != null && !dto.getPatient().getDateoftoday().isEmpty()) {
                patient.setDateoftoday(LocalDate.parse(dto.getPatient().getDateoftoday()));
            } else {
                patient.setDateoftoday(LocalDate.now());
            }
        } catch (DateTimeParseException e) {
            logger.warn("Erreur de format de date: {}", e.getMessage());
            throw new ValidationException("Format de date invalide. Utilisez le format yyyy-MM-dd");
        }

        patient.setWeight(dto.getPatient().getWeight());
        patient.setHeight(dto.getPatient().getHeight());
        patient.setEmail(dto.getPatient().getEmail());
        patient.setNationalIDcardnumber(nationalIdCard);
        patient.setComment(dto.getPatient().getComment());

        // Sauvegarder le patient
        return patientRepo.save(patient);
    }

    /**
     * Analyse la date et l'heure du rendez-vous
     * @param dateStr La chaîne de date
     * @return La date et l'heure du rendez-vous
     * @throws ValidationException Si le format de date est invalide
     */
    private LocalDateTime parseAppointmentDateTime(String dateStr) {
        try {
            // Essayer avec le format date-heure complet
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException e) {
            try {
                // Essayer avec juste la date (ajout de l'heure par défaut: 9h00)
                return LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE).atTime(9, 0);
            } catch (DateTimeParseException ex) {
                logger.warn("Format de date invalide: {}", dateStr);
                throw new ValidationException("Format de date invalide. Utilisez le format ISO (yyyy-MM-ddTHH:mm:ss)");
            }
        }
    }

    /**
     * Vérifie les conflits de rendez-vous pour un médecin
     * @param doctor Le médecin
     * @param dateTime La date et l'heure du rendez-vous
     * @throws ValidationException S'il y a un conflit
     */
    private void checkAppointmentConflicts(Doctor doctor, LocalDateTime dateTime) {
        // Plage de vérification (30 minutes avant et après)
        LocalDateTime startCheck = dateTime.minusMinutes(30);
        LocalDateTime endCheck = dateTime.plusMinutes(30);

        // Vérifier s'il existe déjà un rendez-vous pour ce médecin dans cette plage horaire
        List<AppointementDoctor> conflictingAppointments = appointmentRepo
                .findByDoctorIdAndDateBetweenAndStatusNot(
                        doctor.getId(), startCheck, endCheck, "ANNULÉ");

        if (!conflictingAppointments.isEmpty()) {
            logger.warn("Conflit de rendez-vous détecté pour le médecin {} à la date {}",
                    doctor.getId(), dateTime);
            throw new ValidationException(
                    "Un autre rendez-vous existe déjà pour ce médecin à cette heure. " +
                            "Veuillez choisir un autre horaire.");
        }
    }

    /**
     * Convertit un rendez-vous en DTO de réponse
     * @param appointment Le rendez-vous
     * @param doctor Le médecin
     * @return Le DTO de réponse
     */
    private AppointmentResponseDTO convertToResponseDTO(AppointementDoctor appointment, Doctor doctor) {
        AppointmentResponseDTO responseDTO = new AppointmentResponseDTO();
        responseDTO.setId(appointment.getId());
        responseDTO.setDate(appointment.getDate());
        responseDTO.setStatus(appointment.getStatus());
        responseDTO.setCreatedAt(appointment.getCreatedAt());
        responseDTO.setUpdatedAt(appointment.getUpdatedAt());

        // Informations du médecin
        responseDTO.setDoctor(new AppointmentResponseDTO.DoctorDTO(
                doctor.getId(),
                doctor.getName(),
                doctor.getSpecialization()
        ));

        // Informations du patient
        Patient patient = appointment.getPatient();
        responseDTO.setPatient(new AppointmentResponseDTO.PatientDTO(
                patient.getId(),
                patient.getName(),
                patient.getSurname(),
                patient.getSexe(),
                patient.getDateofbirth() != null ? patient.getDateofbirth().toString() : null,
                patient.getWeight(),
                patient.getHeight(),
                patient.getEmail(),
                patient.getNationalIDcardnumber()
        ));

        return responseDTO;
    }
}