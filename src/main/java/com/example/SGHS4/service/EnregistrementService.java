package com.example.SGHS4.service;
import com.example.SGHS4.entite.Enregistrement;
import java.util.List;
import java.util.Optional;
import com.example.SGHS4.repository.EnregistrementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EnregistrementService {

    private final EnregistrementRepository enregistrementRepository;

    @Autowired
    public EnregistrementService(EnregistrementRepository enregistrementRepository) {
        this.enregistrementRepository = enregistrementRepository;
    }

    public List<Enregistrement> getAllEnregistrements() {
        return enregistrementRepository.findAll();
    }

    public Optional<Enregistrement> getEnregistrementById(Long id) {
        return enregistrementRepository.findById(id);
    }

    public Enregistrement enregistrerPatient(Enregistrement enregistrement) {
        return enregistrementRepository.save(enregistrement);
    }
}



