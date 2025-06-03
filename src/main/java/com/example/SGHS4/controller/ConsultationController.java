package com.example.SGHS4.controller;

import com.example.SGHS4.dto.ConsultationDTO;
import com.example.SGHS4.entite.Consultation;
import com.example.SGHS4.entite.Patient;
import com.example.SGHS4.service.ConsultationService;
import com.example.SGHS4.util.AESUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/consultations")
@CrossOrigin(origins = "*")
public class ConsultationController {

    @Autowired
    private ConsultationService consultationService;

    /**
     * Crée une nouvelle consultation, chiffre les champs sensibles,
     * enregistre dans la BDD, puis retourne un fichier JSON téléchargeable.
     */
    @PostMapping
    public ResponseEntity<byte[]> createAndDownload(@RequestBody ConsultationDTO dto) {
        try {
            // 1. Sauvegarde de la consultation avec chiffrement
            Consultation saved = consultationService.saveConsultationFromDTO(dto);

            // 2. Récupération du patient et de la CNI en clair pour le déchiffrement
            Patient patient = saved.getPatient();
            String encryptedCNI = patient.getEncryptedCNI();
            String cniClear = AESUtil.decryptCNI(encryptedCNI);

            // 3. Déchiffrement des données avant export JSON
            String name = AESUtil.decrypt(saved.getName(), cniClear);
            String surname = AESUtil.decrypt(saved.getSurname(), cniClear);
            String symptome = AESUtil.decrypt(saved.getSymptome(), cniClear);
            String diagnostique = AESUtil.decrypt(saved.getDiagnostique(), cniClear);
            String ordonnance = AESUtil.decrypt(saved.getOrdonnance(), cniClear);
            String commentaire = AESUtil.decrypt(saved.getCommentaire(), cniClear);
            String consultationDate = saved.getConsultationDate().toString(); // Optionnel : à chiffrer aussi si nécessaire

            // 4. Création du JSON
            String jsonContent = "{\n" +
                    "  \"name\": \"" + name + "\",\n" +
                    "  \"surname\": \"" + surname + "\",\n" +
                    "  \"symptome\": \"" + symptome + "\",\n" +
                    "  \"diagnostique\": \"" + diagnostique + "\",\n" +
                    "  \"ordonnance\": \"" + ordonnance + "\",\n" +
                    "  \"commentaire\": \"" + commentaire + "\",\n" +
                    "  \"consultationDate\": \"" + consultationDate + "\"\n" +
                    "}";

            // 5. Préparation du fichier à télécharger
            byte[] fileBytes = jsonContent.getBytes(StandardCharsets.UTF_8);
            String filename = "consultation_" + saved.getId() + ".json";

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(fileBytes);

        } catch (Exception e) {
            String error = "Erreur lors de l'enregistrement de la consultation : " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error.getBytes(StandardCharsets.UTF_8));
        }
    }


    @GetMapping
    public ResponseEntity<List<Consultation>> getAll() {
        return ResponseEntity.ok(consultationService.getAllConsultations());
    }
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Consultation>> getConsultationsForPatient(@PathVariable Long patientId) {
        try {
            List<Consultation> consultations = consultationService.getConsultationsForPatient(patientId);
            return ResponseEntity.ok(consultations);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null); // Retourne une erreur si le patient est introuvable
        }
    }
}
