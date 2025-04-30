package com.example.SGHS4.service;

import com.example.SGHS4.dto.CreateAppointmentDTO;
import com.example.SGHS4.entite.AppointementDoctor;
import com.example.SGHS4.entite.Patient;
import com.example.SGHS4.repository.AppointementDoctorRepository;
import com.example.SGHS4.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class AppointementDoctorService {

    private final AppointementDoctorRepository appointmentRepo;
    private final PatientRepository patientRepo;

    public AppointementDoctorService(AppointementDoctorRepository appointmentRepo,
                                     PatientRepository patientRepo) {
        this.appointmentRepo = appointmentRepo;
        this.patientRepo = patientRepo;
    }

    @Transactional
    public AppointementDoctor createAppointment(CreateAppointmentDTO dto) {
        // Conversion du patient DTO -> Entité
        Patient patient = new Patient();
        patient.setName(dto.getPatient().getName());
        patient.setSurname(dto.getPatient().getSurname());
        patient.setSexe(dto.getPatient().getSexe());
        patient.setDateofbirth(LocalDate.parse(dto.getPatient().getDateofbirth()));
        patient.setWeight(dto.getPatient().getWeight());
        patient.setHeight(dto.getPatient().getHeight());
        patient.setEmail(dto.getPatient().getEmail());
        patient.setNationalIDcardnumber(dto.getPatient().getNationalIDcardnumber());
        patient.setComment(dto.getPatient().getComment());
        patient.setDateoftoday(LocalDate.parse(dto.getPatient().getDateoftoday()));

        Patient savedPatient = patientRepo.save(patient);

        // Création du rendez-vous
        AppointementDoctor appointment = new AppointementDoctor();
        appointment.setPatient(savedPatient);
        appointment.setDate(dto.getDate());
        appointment.setDoctorId(dto.getDoctorId());

        return appointmentRepo.save(appointment);
}
}
