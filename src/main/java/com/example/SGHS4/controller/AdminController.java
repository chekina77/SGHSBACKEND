package com.example.SGHS4.controller;
import com.example.SGHS4.repository.PendingPersonnelRepository; // N'oublie pas l'import

import com.example.SGHS4.dto.AuthentificationDTO;
import com.example.SGHS4.dto.PersonnelDTO;
import com.example.SGHS4.entite.PendingPersonnel;
import com.example.SGHS4.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final PendingPersonnelRepository pendingPersonnelRepository; // ✅ Ajout ici


    public AdminController(AdminService adminService, PendingPersonnelRepository pendingPersonnelRepository) {
        this.adminService = adminService;
        this.pendingPersonnelRepository = pendingPersonnelRepository;
    }

    // Enregistrement du personnel
    @PostMapping("/RegisterPersonnel")
    public ResponseEntity<String> registerPersonnel(@RequestBody PersonnelDTO dto) {
        // Vérification si les informations de personnel (y compris CNI) sont valides
        String message = adminService.registerPersonnel(dto);

        // Si une erreur survient (par exemple : CNI, email, téléphone déjà utilisés)
        if (message.contains("déjà utilisé")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
        }

        // Si tout est en ordre, on retourne une réponse de succès
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @PostMapping("/connexion")
    public ResponseEntity<Map<String, String>> connexion(@RequestBody AuthentificationDTO authenticationDTO) {
        try {
            // Appel au service pour générer les tokens
            Map<String, String> tokens = adminService.connexion(authenticationDTO);
            return ResponseEntity.ok(tokens); // Retourner le JSON contenant les tokens
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Collections.singletonMap("error", "Erreur de connexion : " + e.getMessage()));
        }
    }

    //    Vérification du code et finalisation de l'inscription
    @GetMapping("/pending-personnel/{cni}")
    public ResponseEntity<PendingPersonnel> getPendingPersonnelByCni(@PathVariable String cni) {
        return pendingPersonnelRepository.findByCni(cni)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Finalise l'inscription : récupère la CNI, le code de vérification et le mot de passe,
     * crée l'utilisateur et supprime le PendingPersonnel.
     */
    @PostMapping("/complete-registration")
    public ResponseEntity<String> completeRegistration(@RequestBody Map<String,String> payload) {
        String cni      = payload.get("cni");
        String code     = payload.get("verificationCode");
        String password = payload.get("password");

        String result = adminService.verifyAndCompleteRegistration(cni, code, password);

        return switch (result) {
            case "Code de vérification incorrect." -> ResponseEntity.status(403).body(result);
            case "Personnel non trouvé."              -> ResponseEntity.status(404).body(result);
            default                                   -> ResponseEntity.ok(result);
        };
    }
}