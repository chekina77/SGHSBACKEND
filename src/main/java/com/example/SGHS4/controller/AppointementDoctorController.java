package com.example.SGHS4.controller;

import com.example.SGHS4.entite.AppointementDoctor;
import com.example.SGHS4.service.AppointementDoctorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/appointment")
public class AppointementDoctorController {

    private final AppointementDoctorService appointementDoctorService;

    public AppointementDoctorController(AppointementDoctorService appointementDoctorService) {
        this.appointementDoctorService = appointementDoctorService;
    }

    @GetMapping
    public ResponseEntity<List<AppointementDoctor>> getAllAppointments() {
        List<AppointementDoctor> list = appointementDoctorService.getAllAppointments();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<AppointementDoctor> createAppointment(@RequestBody AppointementDoctor appointment) {
        AppointementDoctor created = appointementDoctorService.createAppointment(appointment);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointementDoctor> getAppointmentById(@PathVariable Long id) {
        Optional<AppointementDoctor> appointment = appointementDoctorService.getAppointmentById(id);
        return appointment.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointementDoctor> updateAppointment(@PathVariable Long id, @RequestBody AppointementDoctor appointmentDetails) {
        Optional<AppointementDoctor> updated = appointementDoctorService.updateAppointment(id, appointmentDetails);
        return updated.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    @GetMapping("/search")
    public ResponseEntity<List<AppointementDoctor>> searchAppointments(@RequestParam String keyword) {
        List<AppointementDoctor> result = appointementDoctorService.searchAppointments(keyword);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
