package com.example.backend_labo2.Service;

import com.example.backend_labo2.Entites.PasswordResetToken;
import com.example.backend_labo2.Entites.User;
import com.example.backend_labo2.Repositories.PasswordResetTokenRepository;
import com.example.backend_labo2.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Transactional
    public void createPasswordResetToken(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isEmpty()) {
            log.warn("Tentative de réinitialisation pour un email inexistant: {}", email);
            return;
        }

        User user = userOptional.get();
        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        resetToken.setUsed(false);

        tokenRepository.save(resetToken);

        String resetLink = frontendUrl + "/reset-password?token=" + token;
        log.info("==============================================");
        log.info("LIEN DE RÉINITIALISATION DE MOT DE PASSE");
        log.info("Pour: {}", email);
        log.info("Lien: {}", resetLink);
        log.info("==============================================");
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(token);

        if (tokenOptional.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = tokenOptional.get();

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now()) || resetToken.isUsed()) {
            return false;
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("Mot de passe réinitialisé pour: {}", user.getEmail());
        return true;
    }

    public boolean validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(token);
        if (tokenOptional.isEmpty()) {
            return false;
        }
        PasswordResetToken resetToken = tokenOptional.get();
        return !resetToken.isUsed() && resetToken.getExpiryDate().isAfter(LocalDateTime.now());
    }
}