package com.example.SGHS4.controller;


import com.example.SGHS4.entite.Appointement;
import com.example.SGHS4.service.AdminAppointementService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

    @RestController
    @RequestMapping("/appointements")
    public class AppointementAdmin {

        private final AdminAppointementService adminappointementService;


        public AppointementAdmin(AdminAppointementService adminappointementService) {
            this.adminappointementService = adminappointementService;
        }

        @GetMapping
        public List<Appointement> getAll() {
            return adminappointementService.getAllAppointments();
        }

        @PostMapping
        public Appointement create(@RequestBody Appointement appointement) {
            return adminappointementService.save(appointement);
        }
        @GetMapping("/search")
        public List<Appointement> search(@RequestParam String keyword) {
            return adminappointementService.searchAppointments(keyword);
        }

    }


