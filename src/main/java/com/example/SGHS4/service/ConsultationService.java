package com.example.SGHS4.service;

import com.example.SGHS4.dto.ConsultationDTO;
import com.example.SGHS4.entite.Consultation;
import com.example.SGHS4.entite.Patient;
import com.example.SGHS4.entite.Utilisateur;
import com.example.SGHS4.repository.ConsultationRepository;
import com.example.SGHS4.repository.PatientRepository;
import com.example.SGHS4.repository.UtilisateurRepository;
import com.example.SGHS4.util.AESUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsultationService {

    @Autowired
    private ConsultationRepository consultationRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    private static final String MASTER_KEY = "master-key-very-secret"; // Même clé si tu veux gérer chiffrement maître

    /**
     * Récupère le CNI en clair d’un patient, en déchiffrant avec la MASTER_KEY
     */
    private String getPatientCNIInClear(Patient patient) throws Exception {
        if (patient.getEncryptedCNI() == null) {
            throw new Exception("CNI chiffrée introuvable pour ce patient");
        }
        // Supposant que la CNI est chiffrée avec MASTER_KEY
        return AESUtil.decrypt(patient.getEncryptedCNI(), MASTER_KEY);
    }

    /**
     * Sauvegarde une consultation à partir d’un DTO
     */
    public List<Consultation> getConsultationsForPatient(Long patientId) throws Exception {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new Exception("Patient introuvable"));

        // Récupérer les consultations triées du plus récent au plus ancien
        return consultationRepository.findByPatientOrderByConsultationDateDesc(patient);
    }

    // Méthode pour enregistrer une nouvelle consultation
    public Consultation saveConsultationFromDTO(ConsultationDTO dto) throws Exception {
        // Récupérer le patient
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new Exception("Patient introuvable"));

        // Récupérer la CNI en clair à partir de la version chiffrée
        String encryptedCNI = patient.getEncryptedCNI();
        if (encryptedCNI == null || encryptedCNI.isEmpty()) {
            throw new RuntimeException("CNI chiffrée manquante !");
        }

        String cniClear = getPatientCNIInClear(patient);
        if (cniClear == null || cniClear.isEmpty()) {
            throw new RuntimeException("CNI déchiffrée invalide !");
        }

        // Création de la consultation avec données entièrement chiffrées
        Consultation consultation = new Consultation();
        consultation.setPatient(patient);
        consultation.setConsultationDate(dto.getConsultationDate()); // Optionnel : à chiffrer si nécessaire

        // 🔐 Chiffrement de toutes les données, y compris nom, prénom et symptôme
        consultation.setName(AESUtil.encrypt(dto.getName(), cniClear));
        consultation.setSurname(AESUtil.encrypt(dto.getSurname(), cniClear));
        consultation.setSymptome(AESUtil.encrypt(dto.getSymptome(), cniClear));
        consultation.setDiagnostique(AESUtil.encrypt(dto.getDiagnostique(), cniClear));
        consultation.setOrdonnance(AESUtil.encrypt(dto.getOrdonnance(), cniClear));
        consultation.setCommentaire(AESUtil.encrypt(dto.getCommentaire(), cniClear));

        // Lier le médecin si présent
        if (dto.getDoctorId() != null) {
            Utilisateur doctor = utilisateurRepository.findById(dto.getDoctorId())
                    .orElseThrow(() -> new Exception("Médecin introuvable"));
            consultation.setDoctor(doctor);
        }

        // Sauvegarde en base
        return consultationRepository.save(consultation);
    }

    /**
     * Récupère une consultation par son ID, en déchiffrant les données sensibles
     */
    public ConsultationDTO getConsultationById(Long id, String cniClear) throws Exception {
        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new Exception("Consultation non trouvée"));

        ConsultationDTO dto = new ConsultationDTO();
        dto.setId(consultation.getId());
        dto.setConsultationDate(consultation.getConsultationDate());
        dto.setPatientId(consultation.getPatient() != null ? consultation.getPatient().getId() : null);
        dto.setDoctorId(consultation.getDoctor() != null ? consultation.getDoctor().getId() : null);
        dto.setName(consultation.getName());
        dto.setSurname(consultation.getSurname());
        dto.setSymptome(consultation.getSymptome());

        // Déchiffrement des champs sensibles
        dto.setDiagnostique(AESUtil.decrypt(consultation.getDiagnostique(), cniClear));
        dto.setOrdonnance(AESUtil.decrypt(consultation.getOrdonnance(), cniClear));
        dto.setCommentaire(AESUtil.decrypt(consultation.getCommentaire(), cniClear));

        return dto;
    }

    /**
     * Récupère toutes les consultations (sans chiffrement)
     */
    public List<Consultation> getAllConsultations() {
        return consultationRepository.findAll();
    }
}
