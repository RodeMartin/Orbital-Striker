package com.orbitalstriker.backend.modules.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false) // Ha nincs beállítva SMTP, ne haljon le
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("noreply@orbitalstriker.com");
                message.setTo(to);
                message.setSubject(subject);
                message.setText(body);
                mailSender.send(message);
                System.out.println("EMAIL ELKÜLDVE IDE: " + to);
            } catch (Exception e) {
                System.err.println("EMAIL HIBA: " + e.getMessage());
            }
        } else {
            System.out.println("--- [EMAIL SZIMULÁCIÓ] ---");
            System.out.println("TO: " + to);
            System.out.println("SUBJECT: " + subject);
            System.out.println("BODY: " + body);
            System.out.println("--------------------------");
        }
    }
}