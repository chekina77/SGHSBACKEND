package com.example.SGHS4.controller;

import com.example.SGHS4.dto.CreateAppointmentDTO;
import com.example.SGHS4.entite.AppointementDoctor;
import com.example.SGHS4.service.AppointementDoctorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/enregistrements")
public class AppointementDoctorController {

    private final AppointementDoctorService appointmentService;

    public AppointementDoctorController(AppointementDoctorService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/enregistrer")
    public ResponseEntity<AppointementDoctor> createAppointment(@RequestBody CreateAppointmentDTO appointmentDTO) {
        AppointementDoctor createdAppointment = appointmentService.createAppointment(appointmentDTO);
        return new ResponseEntity<>(createdAppointment, HttpStatus.CREATED);
}
}