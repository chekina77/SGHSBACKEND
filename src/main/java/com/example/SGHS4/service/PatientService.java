package com.example.SGHS4.service;

import com.example.SGHS4.dto.PatientDTO;
import com.example.SGHS4.entite.Patient;
import com.example.SGHS4.exceptions.ResourceNotFoundException;
import com.example.SGHS4.exceptions.ValidationException;
import com.example.SGHS4.repository.PatientRepository;
import com.example.SGHS4.util.AESUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PatientService {

    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);
    private static final String MASTER_KEY = "master-key-very-secret";

    private final PatientRepository patientRepository;

    @Autowired
    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public PatientDTO createPatient(PatientDTO patientDTO) {
        logger.info("Création d'un nouveau patient");

        validatePatientData(patientDTO);

        if (patientRepository.findByEncryptedCNI(patientDTO.getNationalIDCardNumber()).isPresent()) {
            throw new ValidationException("Un patient avec ce numéro de carte d'identité existe déjà");
        }

        Patient patient = new Patient();
        patient.setName(patientDTO.getName());
        patient.setSurname(patientDTO.getSurname());
        patient.setEmail(patientDTO.getEmail());
        patient.setPhoneNumber(patientDTO.getPhoneNumber());
        patient.setSexe(patientDTO.getSexe());
        patient.setAllergies(patientDTO.getAllergies());
        patient.setComment(patientDTO.getComment());
        patient.setDateOfBirth(patientDTO.getDateOfBirth());
        patient.setWeight(patientDTO.getWeight());
        patient.setHeight(patientDTO.getHeight());
        patient.setEncryptedCNI(patientDTO.getNationalIDCardNumber());
        patient.setDateOfToday(LocalDate.now());
        patient.setMedecinName(patientDTO.getMedecinName());

        Patient savedPatient = patientRepository.save(patient);
        logger.info("Patient créé avec l'ID: {}", savedPatient.getId());

        return convertToDTO(savedPatient);
    }

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

    @Transactional(readOnly = true)
    public List<PatientDTO> getAllPatients() {
        logger.info("Récupération de tous les patients");
        return patientRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PatientDTO updatePatient(Long id, PatientDTO patientDTO) {
        logger.info("Mise à jour du patient avec l'ID: {}", id);

        validatePatientData(patientDTO);

        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Patient avec l'ID {} non trouvé pour la mise à jour", id);
                    return new ResourceNotFoundException("Patient non trouvé avec l'ID: " + id);
                });

        existingPatient.setName(patientDTO.getName());
        existingPatient.setSurname(patientDTO.getSurname());
        existingPatient.setEmail(patientDTO.getEmail());
        existingPatient.setPhoneNumber(patientDTO.getPhoneNumber());
        existingPatient.setSexe(patientDTO.getSexe());
        existingPatient.setAllergies(patientDTO.getAllergies());
        existingPatient.setComment(patientDTO.getComment());
        existingPatient.setDateOfBirth(patientDTO.getDateOfBirth());
        existingPatient.setWeight(patientDTO.getWeight());
        existingPatient.setHeight(patientDTO.getHeight());
        existingPatient.setEncryptedCNI(patientDTO.getNationalIDCardNumber());
        existingPatient.setDateOfToday(patientDTO.getDateOfToday());
        existingPatient.setMedecinName(patientDTO.getMedecinName());

        Patient updatedPatient = patientRepository.save(existingPatient);
        logger.info("Patient avec l'ID {} mis à jour", id);

        return convertToDTO(updatedPatient);
    }

    public void deletePatient(Long id) {
        logger.info("Suppression du patient avec l'ID: {}", id);

        if (!patientRepository.existsById(id)) {
            logger.warn("Patient avec l'ID {} non trouvé pour la suppression", id);
            throw new ResourceNotFoundException("Patient non trouvé avec l'ID: " + id);
        }

        patientRepository.deleteById(id);
        logger.info("Patient avec l'ID {} supprimé", id);
    }

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

    @Transactional(readOnly = true)
    public PatientDTO findByNationalIdCard(String nationalIdCard) {
        logger.info("Recherche d'un patient avec le numéro de carte d'identité: {}", nationalIdCard);

        return patientRepository.findByEncryptedCNI(nationalIdCard)
                .map(this::convertToDTO)
                .orElse(null);
    }


    public List<PatientDTO> getAllPatientsNameAndSurnameDecrypted() {
        return patientRepository.findAll().stream().map(p -> {
            String nameClear = "ERROR";
            String surnameClear = "ERROR";

            try {
                // Étape 1 : déchiffrer la CNI (clé utilisée pour le reste des champs)
                String cni = AESUtil.decryptCNI(p.getEncryptedCNI());

                // Étape 2 : déchiffrer les champs sensibles avec la CNI
                nameClear = AESUtil.decrypt(p.getName(), cni);
                surnameClear = AESUtil.decrypt(p.getSurname(), cni);

            } catch (Exception e) {
                System.err.println("Erreur de déchiffrement pour le patient id=" + p.getId());
                e.printStackTrace();
            }

            return new PatientDTO(p.getId(), nameClear, surnameClear);
        }).collect(Collectors.toList());
    }


    private void validatePatientData(PatientDTO patientDTO) {
        if (patientDTO == null) {
            throw new ValidationException("Les données du patient ne peuvent pas être nulles");
        }
        if (patientDTO.getName() == null || patientDTO.getName().trim().isEmpty()) {
            throw new ValidationException("Le nom du patient est obligatoire");
        }
        if (patientDTO.getSurname() == null || patientDTO.getSurname().trim().isEmpty()) {
            throw new ValidationException("Le prénom du patient est obligatoire");
        }
        if (patientDTO.getDateOfBirth() != null && patientDTO.getDateOfBirth().isAfter(LocalDate.now())) {
            throw new ValidationException("La date de naissance ne peut pas être future");
        }
    }

    private PatientDTO convertToDTO(Patient patient) {
        PatientDTO dto = new PatientDTO();
        dto.setId(patient.getId());
        dto.setName(patient.getName());
        dto.setSurname(patient.getSurname());
        dto.setSexe(patient.getSexe());
        dto.setDateOfBirth(patient.getDateOfBirth());
        dto.setWeight(patient.getWeight());
        dto.setHeight(patient.getHeight());
        dto.setEmail(patient.getEmail());
        dto.setNationalIDCardNumber(patient.getEncryptedCNI());
        dto.setPhoneNumber(patient.getPhoneNumber());
        dto.setAllergies(patient.getAllergies());
        dto.setComment(patient.getComment());
        dto.setDateOfToday(patient.getDateOfToday());
        dto.setMedecinName(patient.getMedecinName());
        return dto;
    }
}
