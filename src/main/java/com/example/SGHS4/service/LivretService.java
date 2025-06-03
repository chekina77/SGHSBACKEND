package com.example.SGHS4.service;

import com.example.SGHS4.dto.LivretDTO;
import com.example.SGHS4.entite.Livret;
import com.example.SGHS4.entite.Patient;
import com.example.SGHS4.entite.Utilisateur;
import com.example.SGHS4.enums.TypeDeRole;
import com.example.SGHS4.repository.LivretRepository;
import com.example.SGHS4.repository.PatientRepository;
import com.example.SGHS4.repository.UtilisateurRepository;
import com.example.SGHS4.util.AESUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
public class LivretService {

    @Autowired
    private LivretRepository livretRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    private static final String MASTER_KEY = "master-key-very-secret"; // même que dans PatientServiceImpl

    private String getCniClear(Patient patient) throws Exception {
        String encryptedCni = patient.getEncryptedCNI();
        if (encryptedCni == null) {
            throw new Exception("CNI chiffrée introuvable pour ce patient");
        }
        return AESUtil.decrypt(encryptedCni, MASTER_KEY);
    }

    public Livret createLivret(LivretDTO livretDTO) throws Exception {
        // Récupération du patient
        Patient patient = patientRepository.findById(livretDTO.getPatientId())
                .orElseThrow(() -> new Exception("Patient non trouvé"));

        // Récupération du médecin
        Utilisateur doctor = utilisateurRepository.findById(livretDTO.getDoctorId())
                .orElseThrow(() -> new Exception("Médecin non trouvé"));

        if (!doctor.getRole().equals(TypeDeRole.MEDECIN)) {
            throw new Exception("L'utilisateur n'est pas un médecin.");
        }

        // Création d'un nouveau livret
        Livret livret = new Livret();
        livret.setPatient(patient);
        livret.setDoctor(doctor);

        // Récupération de la clé de chiffrement à partir du CNI du patient
        String cniClear = getCniClear(patient);

        // Chiffrement des données sensibles
        livret.setDiagnostique(AESUtil.encrypt(livretDTO.getDiagnostique(), cniClear));
        livret.setOrdonnance(AESUtil.encrypt(livretDTO.getOrdonnance(), cniClear));
        livret.setCommentaire(AESUtil.encrypt(livretDTO.getCommentaire(), cniClear));

        // Possibilité de chiffrer aussi la date, si besoin
        livret.setConsultationDate(livretDTO.getConsultationDate());

        // Sauvegarde du livret
        return livretRepository.save(livret);
    }

    private Utilisateur getAuthenticatedUser() throws Exception {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;

        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        } else {
            throw new Exception("Utilisateur non authentifié");
        }

        return utilisateurRepository.findByEmail(username)
                .orElseThrow(() -> new Exception("Utilisateur non trouvé"));
    }

    private List<LivretDTO> mapLivretsToDTOs(List<Livret> livrets, String cni) {
        return livrets.stream().map(livret -> {
            LivretDTO dto = new LivretDTO();
            dto.setId(livret.getId());
            dto.setPatientId(livret.getPatient().getId());
            dto.setPatientName(livret.getPatient().getName());
            dto.setDoctorId(livret.getDoctor().getId());
            dto.setConsultationDate(livret.getConsultationDate());

            try {
                dto.setDiagnostique(AESUtil.decrypt(livret.getDiagnostique(), cni));
                dto.setOrdonnance(AESUtil.decrypt(livret.getOrdonnance(), cni));
                dto.setCommentaire(AESUtil.decrypt(livret.getCommentaire(), cni));
            } catch (Exception e) {
                throw new RuntimeException("Erreur de déchiffrement : " + e.getMessage());
            }

            return dto;
        }).toList();
    }

    @Transactional
    public List<LivretDTO> getLivretsForPatient(Long patientId) throws Exception {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new Exception("Patient non trouvé"));

        // Récupérer la clé maître (depuis config, ici hardcodée)
        String masterKey = "master-key-very-secret";

        // Déchiffrer la clé CNI du patient
        String cniClear = AESUtil.decrypt(patient.getEncryptedCNI(), masterKey);

        List<Livret> livrets = livretRepository.findByPatientOrderByConsultationDateDesc(patient);

        if (livrets.isEmpty()) {
            throw new Exception("Aucun livret trouvé pour ce patient.");
        }

        // Déchiffrement des champs et création des DTO
        return livrets.stream().map(livret -> {
            try {
                String patientName = AESUtil.decrypt(livret.getPatient().getName(), cniClear);

                String diagnostique = AESUtil.decrypt(livret.getDiagnostique(), cniClear);
                String ordonnance = AESUtil.decrypt(livret.getOrdonnance(), cniClear);
                String commentaire = AESUtil.decrypt(livret.getCommentaire(), cniClear);

                LivretDTO dto = new LivretDTO();
                dto.setId(livret.getId());
                dto.setPatientId(patient.getId());
                dto.setPatientName(patientName);
                dto.setDoctorId(livret.getDoctor().getId());
                dto.setDiagnostique(diagnostique);
                dto.setOrdonnance(ordonnance);
                dto.setCommentaire(commentaire);
                dto.setConsultationDate(livret.getConsultationDate());
                dto.setViewCount(livret.getViewCount());
                return dto;
            } catch (Exception e) {
                throw new RuntimeException("Erreur déchiffrement");
            }
        }).toList();
    }



    @Transactional
    public List<LivretDTO> getLivretsForAuthenticatedPatient() throws Exception {
        Utilisateur utilisateur = getAuthenticatedUser();

        Patient patient = patientRepository.findByUtilisateurId(utilisateur.getId())
                .orElseThrow(() -> new Exception("Patient non trouvé pour l'utilisateur connecté"));

        if (!(utilisateur.getRole().equals(TypeDeRole.ADMINISTRATEUR)
                || utilisateur.getRole().equals(TypeDeRole.MEDECIN))) {
            throw new AccessDeniedException("Accès refusé : seuls les médecins ou administrateurs peuvent voir ce livret.");
        }

        List<Livret> livrets = livretRepository.findByPatientOrderByConsultationDateDesc(patient);

        if (livrets.isEmpty()) {
            throw new Exception("Aucun livret trouvé pour ce patient.");
        }

        livrets.forEach(l -> l.setViewCount(l.getViewCount() + 1));
        livretRepository.saveAll(livrets);

        String cni = getCniClear(patient);

        return livrets.stream().map(livret -> {
            LivretDTO dto = new LivretDTO();
            dto.setId(livret.getId());
            dto.setPatientId(patient.getId());
            dto.setPatientName(patient.getName());
            dto.setDoctorId(livret.getDoctor().getId());
            dto.setConsultationDate(livret.getConsultationDate());
            dto.setViewCount(livret.getViewCount());

            try {
                dto.setDiagnostique(AESUtil.decrypt(livret.getDiagnostique(), cni));
                dto.setOrdonnance(AESUtil.decrypt(livret.getOrdonnance(), cni));
                dto.setCommentaire(AESUtil.decrypt(livret.getCommentaire(), cni));
            } catch (Exception e) {
                throw new RuntimeException("Erreur de déchiffrement : " + e.getMessage());
            }
            return dto;
        }).toList();
    }

    public LivretDTO getLivretByPatientId(Long patientId) throws Exception {
        Livret livret = livretRepository.findByPatientId(patientId)
                .orElseThrow(() -> new Exception("Aucun livret trouvé pour ce patient"));

        // Récupérer la clé maître de configuration (ex : depuis application.properties)
        String masterKey = "TaMasterKeySecrete";

        // Déchiffrer la clé CNI
        String cniClear = AESUtil.decrypt(livret.getPatient().getEncryptedCNI(), masterKey);

        // Déchiffrer les champs avec la clé CNI claire
        String diagnostique = AESUtil.decrypt(livret.getDiagnostique(), cniClear);
        String ordonnance = AESUtil.decrypt(livret.getOrdonnance(), cniClear);
        String commentaire = AESUtil.decrypt(livret.getCommentaire(), cniClear);

        // Déchiffrer le nom patient si chiffré, sinon utiliser tel quel
        String patientName = livret.getPatient().getName(); // si déjà clair
        // Ou, si chiffré : patientName = AESUtil.decrypt(livret.getPatient().getName(), cniClear);

        LivretDTO dto = new LivretDTO();
        dto.setId(livret.getId());
        dto.setPatientId(livret.getPatient().getId());
        dto.setPatientName(patientName);
        dto.setDoctorId(livret.getDoctor().getId());
        dto.setDiagnostique(diagnostique);
        dto.setOrdonnance(ordonnance);
        dto.setCommentaire(commentaire);
        dto.setConsultationDate(livret.getConsultationDate());
        dto.setViewCount(livret.getViewCount()); // si besoin

        return dto;
    }

}
