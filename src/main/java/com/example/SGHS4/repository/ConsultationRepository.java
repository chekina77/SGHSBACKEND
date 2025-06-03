package com.example.SGHS4.repository;


import com.example.SGHS4.entite.Consultation;
import com.example.SGHS4.entite.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    // Vous pouvez ajouter ici des méthodes spécifiques à vos besoins, par exemple:
    // List<Consultation> findByDoctorId(Long doctorId);
    // List<Consultation> findByPatientId(Long patientId);
    List<Consultation> findByPatientOrderByConsultationDateDesc(Patient patient);

}
