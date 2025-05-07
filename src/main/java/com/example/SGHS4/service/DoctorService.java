package com.example.SGHS4.service;

import com.example.SGHS4.dto.DoctorDTO;
import com.example.SGHS4.entite.Doctor;
import com.example.SGHS4.exceptions.ResourceNotFoundException;
import com.example.SGHS4.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Couche métier pour gérer les médecins.
 */
@Service
@Transactional
public class DoctorService {

    private final DoctorRepository repo;

    public DoctorService(DoctorRepository repo) {
        this.repo = repo;
    }

    /**
     * Récupère tous les médecins.
     */
    public List<DoctorDTO> getAllDoctors() {
        return repo.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Crée un nouveau médecin.
     */
    public DoctorDTO createDoctor(DoctorDTO dto) {
        Doctor doc = new Doctor();
        doc.setNom(dto.getNom());
        doc.setPrenom(dto.getPrenom());
        doc.setEmail(dto.getEmail());
        doc.setTelephone(dto.getTelephone());
        Doctor saved = repo.save(doc);
        return toDTO(saved);
    }

    /**
     * Recherche un médecin par ID.
     * @throws ResourceNotFoundException si non trouvé.
     */
    public DoctorDTO getDoctorById(Long id) {
        Doctor doc = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Médecin non trouvé id=" + id));
        return toDTO(doc);
    }

    /**
     * Met à jour un médecin existant.
     */
    public DoctorDTO updateDoctor(Long id, DoctorDTO dto) {
        Doctor doc = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Médecin non trouvé id=" + id));
        doc.setNom(dto.getNom());
        doc.setPrenom(dto.getPrenom());
        doc.setEmail(dto.getEmail());
        doc.setTelephone(dto.getTelephone());
        Doctor updated = repo.save(doc);
        return toDTO(updated);
    }

    /**
     * Supprime un médecin par ID.
     */
    public void deleteDoctor(Long id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Médecin non trouvé id=" + id);
        }
        repo.deleteById(id);
    }

    /**
     * Recherche avancée de médecins par mot‑clé dans nom ou prénom.
     */
    public List<DoctorDTO> searchDoctors(String keyword) {
        return repo.findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(keyword, keyword)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convertit une entité Doctor en DoctorDTO.
     */
    private DoctorDTO toDTO(Doctor doc) {
        DoctorDTO dto = new DoctorDTO();
        dto.setId(doc.getId());
        dto.setNom(doc.getNom());
        dto.setPrenom(doc.getPrenom());
        dto.setEmail(doc.getEmail());
        dto.setTelephone(doc.getTelephone());
        return dto;
    }
}