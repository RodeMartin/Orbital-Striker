package com.orbitalstriker.backend.modules.user.controller;

import com.orbitalstriker.backend.modules.user.model.User;
import com.orbitalstriker.backend.modules.user.repository.UserRepository;
import com.orbitalstriker.backend.modules.user.service.EmailService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class PaymentController {

    private final UserRepository userRepository;
    private final EmailService emailService;

    // BATTLE PASS VÉTEL
    @PostMapping("/buy-premium")
    public PaymentResponse buyPremium(@RequestBody PaymentRequest request) {
        return processPayment(request, "PREMIUM");
    }

    // GOLD VÉTEL (ÚJ!)
    @PostMapping("/buy-gold")
    public PaymentResponse buyGold(@RequestBody PaymentRequest request) {
        return processPayment(request, "GOLD");
    }

    private PaymentResponse processPayment(PaymentRequest request, String type) {
        if (request.getCardNumber() == null || request.getCardNumber().length() < 16) {
            return new PaymentResponse(false, "Érvénytelen kártyaszám!");
        }
        
        User user = userRepository.findByUsername(request.getUsername())
                .orElseGet(() -> userRepository.save(User.builder().username(request.getUsername()).build()));
        
        if (type.equals("PREMIUM")) {
            user.setIsPremium(true);
            // Battle Pass mellé adunk ajándék goldot is
            user.setGold(user.getGold() + 1000);
        } else if (type.equals("GOLD")) {
            user.setGold(user.getGold() + 5000); // 5000 Arany
        }
        
        userRepository.save(user);

        // Email
        if (user.getEmail() != null) emailService.sendEmail(user.getEmail(), "Purchase Successful", "Vettél: " + type);

        return new PaymentResponse(true, "Sikeres vásárlás: " + type);
    }

    @Data
    public static class PaymentRequest {
        private String username;
        private String cardNumber;
        private String cvc;
    }

    @Data @AllArgsConstructor
    public static class PaymentResponse {
        private boolean success;
        private String message;
    }
}