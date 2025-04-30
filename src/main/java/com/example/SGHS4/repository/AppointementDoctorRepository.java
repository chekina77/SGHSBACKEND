package com.example.SGHS4.repository;

import com.example.SGHS4.entite.AppointementDoctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointementDoctorRepository extends JpaRepository<AppointementDoctor, Long> {

    /**
     * Trouve les rendez-vous d'un médecin dans une plage de dates
     * @param doctorId ID du médecin
     * @param start Date de début
     * @param end Date de fin
     * @return Liste des rendez-vous
     */
    List<AppointementDoctor> findByDoctorIdAndDateBetween(Long doctorId, LocalDateTime start, LocalDateTime end);

    /**
     * Trouve les rendez-vous d'un médecin dans une plage de dates avec un statut spécifique
     * @param doctorId ID du médecin
     * @param start Date de début
     * @param end Date de fin
     * @param status Statut à exclure
     * @return Liste des rendez-vous
     */
    List<AppointementDoctor> findByDoctorIdAndDateBetweenAndStatusNot(
            Long doctorId, LocalDateTime start, LocalDateTime end, String status);

    /**
     * Trouve les rendez-vous d'un patient
     * @param patientId ID du patient
     * @return Liste des rendez-vous
     */
    List<AppointementDoctor> findByPatientId(Long patientId);

    /**
     * Trouve les rendez-vous à venir d'un patient
     * @param patientId ID du patient
     * @param now Date/heure actuelle
     * @return Liste des rendez-vous
     */
    List<AppointementDoctor> findByPatientIdAndDateAfterOrderByDateAsc(Long patientId, LocalDateTime now);

    /**
     * Trouve les rendez-vous par statut
     * @param status Statut du rendez-vous
     * @return Liste des rendez-vous
     */
    List<AppointementDoctor> findByStatus(String status);

    /**
     * Trouve les rendez-vous proches dans le temps
     * @param start Date/heure de début
     * @param end Date/heure de fin
     * @return Liste des rendez-vous
     */
    @Query("SELECT a FROM AppointementDoctor a WHERE a.date BETWEEN :start AND :end ORDER BY a.date ASC")
    List<AppointementDoctor> findUpcomingAppointments(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Compte le nombre de rendez-vous par médecin pour une journée donnée
     * @param start Début de la journée
     * @param end Fin de la journée
     * @return Liste des comptages par médecin
     */
    @Query("SELECT a.doctorId, COUNT(a) FROM AppointementDoctor a " +
            "WHERE a.date BETWEEN :start AND :end AND a.status != 'ANNULÉ' " +
            "GROUP BY a.doctorId")
    List<Object[]> countAppointmentsByDoctor(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Trouve les rendez-vous qui se chevauchent pour un médecin
     * @param doctorId ID du médecin
     * @param appointmentId ID du rendez-vous à exclure
     * @param start Début de la plage horaire
     * @param end Fin de la plage horaire
     * @return Liste des rendez-vous en conflit
     */
    @Query("SELECT a FROM AppointementDoctor a " +
            "WHERE a.doctorId = :doctorId " +
            "AND a.id != :appointmentId " +
            "AND a.status != 'ANNULÉ' " +
            "AND a.date BETWEEN :start AND :end")
    List<AppointementDoctor> findOverlappingAppointments(
            @Param("doctorId") Long doctorId,
            @Param("appointmentId") Long appointmentId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}