package com.example.SGHS4.repository;


import com.example.SGHS4.entite.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    /**
     * Trouve un médecin par son nom
     * @param name Nom du médecin
     * @return Médecin trouvé
     */
    Optional<Doctor> findByName(String name);

    /**
     * Trouve les médecins par spécialisation
     * @param specialization Spécialisation
     * @return Liste des médecins
     */
    List<Doctor> findBySpecialization(String specialization);

    /**
     * Trouve les médecins disponibles à une date donnée
     * @param date Date de recherche
     * @return Liste des médecins disponibles
     */
    @Query("SELECT d FROM Doctor d WHERE d.id NOT IN " +
            "(SELECT a.doctorId FROM AppointementDoctor a WHERE DATE(a.date) = :date AND a.status != 'ANNULÉ')")
    List<Doctor> findAvailableDoctorsByDate(@Param("date") LocalDate date);

    /**
     * Trouve les médecins par recherche de texte
     * @param searchTerm Terme de recherche
     * @return Liste des médecins correspondants
     */
    @Query("SELECT d FROM Doctor d WHERE " +
            "LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(d.specialization) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Doctor> searchDoctors(@Param("searchTerm") String searchTerm);

    /**
     * Vérifie si un médecin est disponible à un moment précis
     * @param doctorId ID du médecin
     * @param start Début de la plage horaire
     * @param end Fin de la plage horaire
     * @return true si disponible, false sinon
     */
    @Query("SELECT COUNT(a) = 0 FROM AppointementDoctor a " +
            "WHERE a.doctorId = :doctorId " +
            "AND a.status != 'ANNULÉ' " +
            "AND ((a.date <= :end AND a.date >= :start) OR " +
            "(DATEADD(MINUTE, 30, a.date) >= :start AND DATEADD(MINUTE, 30, a.date) <= :end))")
    boolean isDoctorAvailableAtTime(
            @Param("doctorId") Long doctorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Compte le nombre de rendez-vous par spécialisation pour une période donnée
     * @param start Début de la période
     * @param end Fin de la période
     * @return Liste de spécialisations avec le nombre de rendez-vous
     */
    @Query("SELECT d.specialization, COUNT(a) FROM Doctor d " +
            "JOIN AppointementDoctor a ON d.id = a.doctorId " +
            "WHERE a.date BETWEEN :start AND :end " +
            "AND a.status != 'ANNULÉ' " +
            "GROUP BY d.specialization " +
            "ORDER BY COUNT(a) DESC")
    List<Object[]> countAppointmentsBySpecialization(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}