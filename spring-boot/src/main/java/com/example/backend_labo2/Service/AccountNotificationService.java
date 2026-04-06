package com.example.backend_labo2.Service;

import com.example.backend_labo2.Entites.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class AccountNotificationService {

    private final JavaMailSender mailSender;
    private final String mailUsername;
    private final boolean mailEnabled;

    public AccountNotificationService(
            @Autowired(required = false) JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String mailUsername
    ) {
        this.mailSender = mailSender;
        this.mailUsername = mailUsername;
        this.mailEnabled = mailSender != null && !mailUsername.isEmpty();
    }

    // Appelée par UserService.register()
    public void sendAccountCreatedEmail(User user) {
        if (!mailEnabled) {
            System.out.println("[MAIL DÉSACTIVÉ] Notification compte créé pour : " + user.getEmail());
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailUsername);
            message.setTo(user.getEmail());
            message.setSubject("Bienvenue sur LabManager - Compte créé");
            message.setText(
                "Bonjour " + user.getPrenom() + " " + user.getNom() + ",\n\n" +
                "Votre compte a été créé avec succès sur LabManager.\n\n" +
                "Rôle : " + user.getRole().name() + "\n" +
                "Email : " + user.getEmail() + "\n\n" +
                "Vous pouvez maintenant vous connecter.\n\n" +
                "Cordialement,\nL'équipe LabManager"
            );
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("[MAIL ERREUR] Impossible d'envoyer l'email à " + user.getEmail() + " : " + e.getMessage());
        }
    }
}