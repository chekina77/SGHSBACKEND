package com.example.SGHS4.service;

import com.example.SGHS4.dto.LivretDTO;
import com.example.SGHS4.entite.Livret;
import com.example.SGHS4.entite.Patient;
import com.example.SGHS4.entite.Utilisateur;  // Utilisateur au lieu de Doctor
import com.example.SGHS4.enums.TypeDeRole;
import com.example.SGHS4.repository.LivretRepository;
import com.example.SGHS4.repository.PatientRepository;
import com.example.SGHS4.repository.UtilisateurRepository;  // UtilisateurRepository pour trouver les médecins
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class LivretService {

    @Autowired
    private LivretRepository livretRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;  // UtilisateurRepository pour accéder aux médecins

    @Autowired
    private JavaMailSender javaMailSender;

    public Livret createOrUpdateLivret(LivretDTO livretDTO) throws Exception {
        Patient patient = patientRepository.findById(livretDTO.getPatientId())
                .orElseThrow(() -> new Exception("Patient non trouvé"));

        // Chercher un médecin parmi les utilisateurs
        Utilisateur doctor = utilisateurRepository.findById(livretDTO.getDoctorId())
                .orElseThrow(() -> new Exception("Médecin non trouvé"));

        // Assurez-vous que l'utilisateur est bien un médecin
        if (!doctor.getRole().equals(TypeDeRole.MEDECIN)) {
            throw new Exception("L'utilisateur n'est pas un médecin.");
        }

        Livret livret = livretRepository.findByPatient(patient).orElse(new Livret());
        livret.setPatient(patient);
        livret.setDoctor(doctor);  // Associe le médecin à l'utilisateur
        livret.setDiagnostique(livretDTO.getDiagnostique());
        livret.setOrdonnance(livretDTO.getOrdonnance());
        livret.setCommentaire(livretDTO.getCommentaire());
        livret.setConsultationDate(livretDTO.getConsultationDate());

        Livret savedLivret = livretRepository.save(livret);

        // Envoie l'email de confirmation de consultation
        sendConsultationEmail(patient, doctor, livret);

        return savedLivret;
    }

    private void sendConsultationEmail(Patient patient, Utilisateur doctor, Livret livret) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(patient.getEmail());
        message.setSubject("Consultation Médicale");
        message.setText(String.format("Bonjour %s,\n\nVous avez eu une consultation avec Dr. %s.\n\nDiagnostique: %s\nOrdonnance: %s\nCommentaire: %s\nDate de Consultation: %s",
                patient.getName(), doctor.getNom(), livret.getDiagnostique(), livret.getOrdonnance(), livret.getCommentaire(), livret.getConsultationDate()));

        javaMailSender.send(message);
    }
}
