package com.example.SGHS4.service;

import com.example.SGHS4.dto.LabResultDTO;
import com.example.SGHS4.entite.LabResult;
import com.example.SGHS4.entite.Patient;
import com.example.SGHS4.entite.Utilisateur;
import com.example.SGHS4.repository.LabResultRepository;
import com.example.SGHS4.repository.PatientRepository;
import com.example.SGHS4.repository.UtilisateurRepository;
import com.example.SGHS4.util.AESUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LabResultService {

    private static final String MASTER_KEY = "master-key-very-secret";

    @Autowired
    private LabResultRepository labResultRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Déchiffre la CNI du patient avec la clé maître
    private String getPatientCNIInClear(Patient patient) throws Exception {
        if (patient.getEncryptedCNI() == null || patient.getEncryptedCNI().isEmpty()) {
            throw new Exception("CNI chiffrée introuvable pour ce patient");
        }
        // Ici on déchiffre avec la master key comme dans AppointmentServiceImpl
        return AESUtil.decrypt(patient.getEncryptedCNI(), MASTER_KEY);
    }

    // Convertit un LabResult en DTO avec déchiffrement
    public LabResultDTO convertToDTO(LabResult labResult, String cniClear) throws Exception {
        LabResultDTO dto = new LabResultDTO();
        dto.setId(labResult.getId());
        dto.setPatientId(labResult.getPatient() != null ? labResult.getPatient().getId() : null);
        dto.setLaborantinId(labResult.getLaborantin() != null ? labResult.getLaborantin().getId() : null);
        dto.setMedecinId(labResult.getMedecin() != null ? labResult.getMedecin().getId() : null);
        dto.setCreatedAt(labResult.getCreatedAt());

        // Déchiffrement avec la clé cniClear
        dto.setTestName(AESUtil.decrypt(labResult.getTestName(), cniClear));
        dto.setResult(AESUtil.decrypt(labResult.getResult(), cniClear));
        dto.setComment(AESUtil.decrypt(labResult.getComment(), cniClear));

        return dto;
    }

    // Enregistre un LabResult depuis un DTO, en chiffrant les données sensibles
    @Transactional
    public LabResult saveLabResultFromDTO(LabResultDTO dto) throws Exception {
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new Exception("Patient introuvable"));

        String cniClear = getPatientCNIInClear(patient);

        LabResult labResult = new LabResult();
        labResult.setPatient(patient);
        labResult.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now());

        if (dto.getLaborantinId() != null) {
            Utilisateur laborantin = utilisateurRepository.findById(dto.getLaborantinId())
                    .orElseThrow(() -> new Exception("Laborantin introuvable"));
            labResult.setLaborantin(laborantin);
        }

        if (dto.getMedecinId() != null) {
            Utilisateur medecin = utilisateurRepository.findById(dto.getMedecinId())
                    .orElseThrow(() -> new Exception("Médecin introuvable"));
            labResult.setMedecin(medecin);
        }

        // Chiffrement avec la clé cniClear (CNI déchiffrée)
        labResult.setTestName(AESUtil.encrypt(dto.getTestName(), cniClear));
        labResult.setResult(AESUtil.encrypt(dto.getResult(), cniClear));
        labResult.setComment(AESUtil.encrypt(dto.getComment(), cniClear));

        LabResult saved = labResultRepository.save(labResult);

        // Notification WebSocket vers le médecin
        if (saved.getMedecin() != null) {
            LabResultDTO notif = new LabResultDTO();
            notif.setId(saved.getId());
            notif.setPatientId(saved.getPatient().getId());
            notif.setMedecinId(saved.getMedecin().getId());
            notif.setCreatedAt(saved.getCreatedAt());
            notif.setTestName(dto.getTestName());
            notif.setResult(dto.getResult());
            notif.setComment(dto.getComment());

            messagingTemplate.convertAndSend("/topic/labresults/" + saved.getMedecin().getId(), notif);
        }

        return saved;
    }

    // Crée un résultat de laboratoire et retourne le DTO en clair
    public LabResultDTO createAndReturnDTO(LabResultDTO dto) throws Exception {
        LabResult saved = saveLabResultFromDTO(dto);
        String cniClear = getPatientCNIInClear(saved.getPatient());
        return convertToDTO(saved, cniClear);
    }

    // Récupère un résultat de labo par ID, déchiffré
    public LabResultDTO getLabResultById(Long id) throws Exception {
        LabResult labResult = labResultRepository.findById(id)
                .orElseThrow(() -> new Exception("LabResult non trouvé"));

        String cniClear = getPatientCNIInClear(labResult.getPatient());

        return convertToDTO(labResult, cniClear);
    }

    // Récupère tous les résultats d’un patient, déchiffrés
    public List<LabResultDTO> getAllLabResultsDecrypted(Long patientId) throws Exception {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new Exception("Patient introuvable"));

        String cniClear = getPatientCNIInClear(patient);

        List<LabResult> results = labResultRepository.findByPatientOrderByCreatedAtDesc(patient);

        return results.stream()
                .map(result -> {
                    try {
                        return convertToDTO(result, cniClear);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

}
