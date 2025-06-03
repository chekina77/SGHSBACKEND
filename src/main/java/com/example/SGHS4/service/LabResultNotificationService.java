package com.example.SGHS4.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

// Service d'envoi de notifications
@Service
public class LabResultNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public LabResultNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyMedecin(Long medecinId, Object labResultData) {
        String destination = "/topic/labresults/" + medecinId;
        messagingTemplate.convertAndSend(destination, labResultData);
        System.out.println("Notification envoyée au médecin " + medecinId);
    }
}

