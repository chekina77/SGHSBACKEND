package com.example.SGHS4.service;

import com.example.SGHS4.dto.PatientDTO;
import com.example.SGHS4.entite.Patient;
import com.example.SGHS4.exceptions.ResourceNotFoundException;
import com.example.SGHS4.exceptions.ValidationException;
import com.example.SGHS4.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PatientService {

    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;

    @Autowired
    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    /**
     * Crée un nouveau patient
     *
     * @param patientDTO Les données du patient à créer
     * @return Le patient créé
     * @throws ValidationException Si les données du patient sont invalides
     */
    public PatientDTO createPatient(PatientDTO patientDTO) {
        logger.info("Création d'un nouveau patient");

        // Validation des données
        validatePatientData(patientDTO);

        // Vérifier si le patient existe déjà par numéro de carte d'identité nationale


        // Conversion du DTO en entité
        Patient patient = convertToEntity(patientDTO);

        // Sauvegarde de l'entité
        Patient savedPatient = patientRepository.save(patient);
        logger.info("Patient créé avec l'ID: {}", savedPatient.getId());

        // Conversion de l'entité sauvegardée en DTO
        return convertToDTO(savedPatient);
    }

    /**
     * Récupère un patient par son ID
     *
     * @param id L'ID du patient
     * @return Le patient trouvé
     * @throws ResourceNotFoundException Si le patient n'est pas trouvé
     */
    @Transactional(readOnly = true)
    public PatientDTO getPatientById(Long id) {
        logger.info("Recherche du patient avec l'ID: {}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Patient avec l'ID {} non trouvé", id);
                    return new ResourceNotFoundException("Patient non trouvé avec l'ID: " + id);
                });

        return convertToDTO(patient);
    }

    /**
     * Récupère tous les patients
     *
     * @return Liste de tous les patients
     */
    @Transactional(readOnly = true)
    public List<PatientDTO> getAllPatients() {
        logger.info("Récupération de tous les patients");

        List<Patient> patients = patientRepository.findAll();
        logger.info("{} patients trouvés", patients.size());

        return patients.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Met à jour un patient existant
     *
     * @param id         L'ID du patient à mettre à jour
     * @param patientDTO Les nouvelles données du patient
     * @return Le patient mis à jour
     * @throws ResourceNotFoundException Si le patient n'est pas trouvé
     * @throws ValidationException       Si les données du patient sont invalides
     */
    public PatientDTO updatePatient(Long id, PatientDTO patientDTO) {
        logger.info("Mise à jour du patient avec l'ID: {}", id);

        // Validation des données
        validatePatientData(patientDTO);

        // Vérifier si le patient existe
        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Patient avec l'ID {} non trouvé pour la mise à jour", id);
                    return new ResourceNotFoundException("Patient non trouvé avec l'ID: " + id);
                });


        // Mise à jour des champs
        updatePatientFromDTO(existingPatient, patientDTO);

        // Sauvegarde des modifications
        Patient updatedPatient = patientRepository.save(existingPatient);
        logger.info("Patient avec l'ID {} mis à jour", id);

        return convertToDTO(updatedPatient);
    }

    /**
     * Supprime un patient
     *
     * @param id L'ID du patient à supprimer
     * @throws ResourceNotFoundException Si le patient n'est pas trouvé
     */
    public void deletePatient(Long id) {
        logger.info("Suppression du patient avec l'ID: {}", id);

        // Vérifier si le patient existe
        if (!patientRepository.existsById(id)) {
            logger.warn("Patient avec l'ID {} non trouvé pour la suppression", id);
            throw new ResourceNotFoundException("Patient non trouvé avec l'ID: " + id);
        }

        patientRepository.deleteById(id);
        logger.info("Patient avec l'ID {} supprimé", id);
    }

    /**
     * Recherche des patients par nom ou prénom
     *
     * @param query Terme de recherche
     * @return Liste des patients correspondants
     */
    @Transactional(readOnly = true)
    public List<PatientDTO> searchPatients(String query) {
        logger.info("Recherche de patients avec le terme: {}", query);

        if (query == null || query.trim().isEmpty()) {
            logger.warn("Terme de recherche vide, retourne tous les patients");
            return getAllPatients();
        }

        String trimmedQuery = query.trim();
        List<Patient> patients = patientRepository
                .findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCase(trimmedQuery, trimmedQuery);

        logger.info("{} patients trouvés pour le terme '{}'", patients.size(), trimmedQuery);

        return patients.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Recherche un patient par son numéro de carte d'identité
     *
     * @param nationalIdCard Numéro de carte d'identité
     * @return Le patient trouvé ou null
     */
    @Transactional(readOnly = true)
    public PatientDTO findByNationalIdCard(String nationalIdCard) {
        logger.info("Recherche d'un patient avec le numéro de carte d'identité: {}", nationalIdCard);

        return patientRepository.findByNationalIDcardnumber(nationalIdCard)
                .map(this::convertToDTO)
                .orElse(null);
    }

    /**
     * Valide les données d'un patient
     *
     * @param patientDTO DTO à valider
     * @throws ValidationException Si les données sont invalides
     */
    private void validatePatientData(PatientDTO patientDTO) {
        if (patientDTO == null) {
            throw new ValidationException("Les données du patient ne peuvent pas être nulles");
        }

        if (patientDTO.getNom() == null || patientDTO.getNom().trim().isEmpty()) {
            throw new ValidationException("Le nom du patient est obligatoire");
        }

        if (patientDTO.getPrenom() == null || patientDTO.getPrenom().trim().isEmpty()) {
            throw new ValidationException("Le prénom du patient est obligatoire");
        }

        if (patientDTO.getDateNaissance() != null && patientDTO.getDateNaissance().isAfter(LocalDate.now())) {
            throw new ValidationException("La date de naissance ne peut pas être future");
        }

        // Autres validations spécifiques selon les besoins
    }

    /**
     * Convertit un DTO en entité
     */
    private Patient convertToEntity(PatientDTO patientDTO) {
        if (patientDTO.getNom() == null || patientDTO.getNom().isEmpty()) {
            throw new ValidationException("Le nom du patient est obligatoire");
        }

        Patient patient = new Patient();
        patient.setName(patientDTO.getNom());
        patient.setSurname(patientDTO.getPrenom());
        patient.setDateOfBirth(patientDTO.getDateNaissance());
        patient.setSexe(patientDTO.getSexe());
        patient.setPhoneNumber(patientDTO.getTelephone());
        patient.setEmail(patientDTO.getEmail());
        patient.setComment(patientDTO.getAntecedentsMedicaux());
        patient.setAllergies(patientDTO.getAllergies());
        patient.setDateOfToday(LocalDate.now());

        return patient;
    }


    /**
     * Met à jour une entité à partir d'un DTO
     */
    private void updatePatientFromDTO(Patient patient, PatientDTO patientDTO) {
        patient.setName(patientDTO.getNom());
        patient.setSurname(patientDTO.getPrenom());
        patient.setDateOfBirth(patientDTO.getDateNaissance());
        patient.setSexe(patientDTO.getSexe());
        patient.setPhoneNumber(patientDTO.getTelephone());
        patient.setEmail(patientDTO.getEmail());
        patient.setComment(patientDTO.getAntecedentsMedicaux());
        patient.setAllergies(patientDTO.getAllergies());
        patient.setDateOfToday(LocalDate.now());
    }


    /**
     * Convertit une entité en DTO
     */
    private PatientDTO convertToDTO(Patient patient) {
        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setId(patient.getId());
        patientDTO.setNom(patient.getName());
        patientDTO.setPrenom(patient.getSurname());
        patientDTO.setDateNaissance(patient.getDateOfBirth());
        patientDTO.setSexe(patient.getSexe());
        patientDTO.setTelephone(patient.getPhoneNumber());
        patientDTO.setEmail(patient.getEmail());
        patientDTO.setAntecedentsMedicaux(patient.getComment());
        patientDTO.setAllergies(patient.getAllergies());
        patientDTO.setDateInscription(patient.getDateOfToday());
        return patientDTO;
    }
}
