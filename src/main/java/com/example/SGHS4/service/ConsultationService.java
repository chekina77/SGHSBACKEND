package com.example.SGHS4.service;

import com.example.SGHS4.dto.ConsultationDTO;
import com.example.SGHS4.entite.Consultation;
import com.example.SGHS4.entite.Livret;
import com.example.SGHS4.entite.Patient;
import com.example.SGHS4.entite.Utilisateur;
import com.example.SGHS4.enums.TypeDeRole;
import com.example.SGHS4.repository.ConsultationRepository;
import com.example.SGHS4.repository.LivretRepository;
import com.example.SGHS4.repository.PatientRepository;
import com.example.SGHS4.repository.UtilisateurRepository;
import com.example.SGHS4.securiter.AesEncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ConsultationService {

    @Autowired private ConsultationRepository consultationRepository;
    @Autowired private LivretRepository livretRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private UtilisateurRepository utilisateurRepository;
    @Autowired private EmailService emailService;

    public Consultation createConsultation(ConsultationDTO dto) {
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient non trouvé"));

        Utilisateur medecin = utilisateurRepository.findById(dto.getDoctorId())
                .filter(u -> u.getRole() == TypeDeRole.MEDECIN)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé"));

        String fingerprintKey = patient.getFingerprintHash();
        if (fingerprintKey == null || fingerprintKey.length() < 32) {
            throw new RuntimeException("Clé AES invalide ou manquante (empreinte)");
        }
        String aesKey = fingerprintKey.substring(0, 32);

        Consultation c = new Consultation();
        try {
            c.setName(AesEncryptionUtil.encrypt(dto.getName(), aesKey));
            c.setSurname(AesEncryptionUtil.encrypt(dto.getSurname(), aesKey));
            c.setSymptome(AesEncryptionUtil.encrypt(dto.getSymptome(), aesKey));
            c.setDiagnostique(AesEncryptionUtil.encrypt(dto.getDiagnostique(), aesKey));
            c.setOrdonnance(AesEncryptionUtil.encrypt(dto.getOrdonance(), aesKey));
            c.setCommentaire(AesEncryptionUtil.encrypt(dto.getCommentaire(), aesKey));
        } catch (Exception e) {
            throw new RuntimeException("Erreur de chiffrement AES : " + e.getMessage());
        }

        c.setConsultationDate(dto.getConsultationDate());
        c.setDoctor(medecin);
        c = consultationRepository.save(c);

        Livret livret = livretRepository.findByPatient(patient)
                .orElseGet(() -> {
                    Livret newLivret = new Livret();
                    newLivret.setPatient(patient);
                    return livretRepository.save(newLivret);
                });
        livret.addConsultation(c);
        livretRepository.save(livret);

        emailService.sendConsultationEmail(c); // Envoie les données chiffrées
        return c;
    }


    public List<Consultation> getAllConsultations() {
        return consultationRepository.findAll();
    }
}
