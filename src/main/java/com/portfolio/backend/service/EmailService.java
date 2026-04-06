package com.portfolio.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false) // Not failing if properties are incomplete
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public boolean sendOtpEmail(String toEmail, String otp) {
        try {
            System.out.println("==================================================");
            System.out.println("MOCK EMAIL SENDER - OTP FOR " + toEmail + " IS: " + otp);
            System.out.println("==================================================");

            if (mailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                if (fromEmail != null && !fromEmail.isEmpty()) {
                    message.setFrom(fromEmail);
                }
                message.setTo(toEmail);
                message.setSubject("Password Reset OTP - Student Portfolio");
                message.setText("Your password reset OTP is: " + otp + "\nThis OTP is valid for 10 minutes.");
                
                mailSender.send(message);
                return true;
            } else {
                System.out.println("JavaMailSender is null. Ensure spring.mail properties are set.");
            }
        } catch (Exception e) {
            System.err.println("Failed to send email. Check SMTP configuration in application.properties: " + e.getMessage());
            // We return false here instead of true, so we can handle errors accurately if needed
            return false;
        }
        return true; 
    }
}
