package com.portfolio.backend.controller;

import com.portfolio.backend.entity.User;
import com.portfolio.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.time.LocalDateTime;

import com.portfolio.backend.entity.PasswordResetToken;
import com.portfolio.backend.repository.PasswordResetTokenRepository;
import com.portfolio.backend.service.EmailService;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @PostMapping("/signup")
    public ResponseEntity<User> signup(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User loginRequest) {
        Optional<User> user = userRepository.findByEmailAndPassword(loginRequest.getEmail(),
                loginRequest.getPassword());
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }
        return ResponseEntity.status(401).build();
    }

    @GetMapping("/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setName(userDetails.getName());
            user.setSummary(userDetails.getSummary());
            userRepository.save(user);
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/forgot-password")
    @Transactional
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "User not found");
                return ResponseEntity.badRequest().body(response);
            }

            User user = userOpt.get();
            String otp = String.format("%06d", new Random().nextInt(999999));

            Optional<PasswordResetToken> existingTokenOpt = tokenRepository.findByUser(user);

            PasswordResetToken token;
            if (existingTokenOpt.isPresent()) {
                token = existingTokenOpt.get();
                token.setToken(otp);
                token.setExpiryDate(LocalDateTime.now().plusMinutes(10));
            } else {
                token = new PasswordResetToken(otp, user, LocalDateTime.now().plusMinutes(10));
            }
            tokenRepository.save(token);

            boolean emailSent = false;
            try {
                emailSent = emailService.sendOtpEmail(user.getEmail(), otp);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Map<String, String> outResponse = new HashMap<>();
            if (!emailSent) {
                outResponse.put("message", "Failed to send email. Check SMTP configuration.");
                return ResponseEntity.status(500).body(outResponse);
            }

            outResponse.put("message", "OTP generated and sent successfully. Check your email.");
            return ResponseEntity.ok(outResponse);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errResponse = new HashMap<>();
            errResponse.put("message", "Backend Error: " + e.getMessage());
            return ResponseEntity.status(500).body(errResponse);
        }
    }

    @PostMapping("/reset-password")
    @Transactional
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        User user = userOpt.get();
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(otp);

        Map<String, String> resetResponse = new HashMap<>();

        if (tokenOpt.isEmpty() || !tokenOpt.get().getUser().getId().equals(user.getId())) {
            resetResponse.put("message", "Invalid OTP");
            return ResponseEntity.badRequest().body(resetResponse);
        }

        if (tokenOpt.get().getExpiryDate().isBefore(LocalDateTime.now())) {
            resetResponse.put("message", "OTP has expired");
            return ResponseEntity.badRequest().body(resetResponse);
        }

        user.setPassword(newPassword);
        userRepository.save(user);

        tokenRepository.deleteByUser(user);

        resetResponse.put("message", "Password reset successfully");
        return ResponseEntity.ok(resetResponse);
    }
}