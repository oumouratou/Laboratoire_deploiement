package com.example.backend_labo2.Controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

import com.example.backend_labo2.Entites.User;
import com.example.backend_labo2.Service.NotificationService;
import com.example.backend_labo2.Repositories.UserRepository;
import com.example.backend_labo2.DTO.NotificationDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Map;
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    // ================= TECHNICIEN - Non lues uniquement (pour la cloche) =================
    @GetMapping("/technicien")
    public List<NotificationDTO> getNotificationsTechnicien(Principal principal) {
        try {
            if (principal == null || principal.getName() == null) {
                return Collections.emptyList();
            }
            User tech = userRepository.findByEmail(principal.getName()).orElse(null);
            if (tech == null) return Collections.emptyList();

            return notificationService.getUnreadNotifications(tech.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // ================= ETUDIANT - Non lues (pour la cloche) =================
    @GetMapping("/etudiant")
    public List<NotificationDTO> getNotificationsEtudiant(Principal principal) {
        try {
            if (principal == null || principal.getName() == null) {
                return Collections.emptyList();
            }
            User etudiant = userRepository.findByEmail(principal.getName()).orElse(null);
            if (etudiant == null) return Collections.emptyList();

            return notificationService.getUnreadNotifications(etudiant.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // ================= ENSEIGNANT - Non lues (pour la cloche) =================
    @GetMapping("/enseignant")
    public List<NotificationDTO> getNotificationsEnseignant(Principal principal) {
        try {
            if (principal == null || principal.getName() == null) {
                return Collections.emptyList();
            }
            User enseignant = userRepository.findByEmail(principal.getName()).orElse(null);
            if (enseignant == null) return Collections.emptyList();

            return notificationService.getUnreadNotifications(enseignant.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // ================= TOUTES LES NOTIFICATIONS ETUDIANT (lues + non lues) =================
    @GetMapping("/etudiant/all")
    public List<NotificationDTO> getAllNotificationsEtudiant(Principal principal) {
        try {
            if (principal == null || principal.getName() == null) {
                return Collections.emptyList();
            }
            User etudiant = userRepository.findByEmail(principal.getName()).orElse(null);
            if (etudiant == null) return Collections.emptyList();

            return notificationService.getAllNotificationsForUser(etudiant.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // ================= TOUTES LES NOTIFICATIONS ENSEIGNANT (lues + non lues) =================
    @GetMapping("/enseignant/all")
    public List<NotificationDTO> getAllNotificationsEnseignant(Principal principal) {
        try {
            if (principal == null || principal.getName() == null) {
                return Collections.emptyList();
            }
            User enseignant = userRepository.findByEmail(principal.getName()).orElse(null);
            if (enseignant == null) return Collections.emptyList();

            return notificationService.getAllNotificationsForUser(enseignant.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // ================= TOUTES LES NOTIFICATIONS TECHNICIEN (lues + non lues) =================
    @GetMapping("/technicien/all")
    public List<NotificationDTO> getAllNotificationsTechnicien(Principal principal) {
        try {
            if (principal == null || principal.getName() == null) {
                return Collections.emptyList();
            }
            User tech = userRepository.findByEmail(principal.getName()).orElse(null);
            if (tech == null) return Collections.emptyList();

            return notificationService.getAllNotificationsForUser(tech.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // ================= MARQUER COMME LUE =================
    @PutMapping("/read/{id}")
    public void markRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
    }

    // ================= MARQUER TOUTES COMME LUES =================
    @PutMapping("/read-all")
    public void markAllAsRead(Principal principal) {
        try {
            if (principal == null || principal.getName() == null) return;
            User user = userRepository.findByEmail(principal.getName()).orElse(null);
            if (user == null) return;
            
            notificationService.markAllAsReadForUser(user.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
 // DTO simple pour le body { "message": "..." }
    public static class AlertRequest {
        private String message;
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    // ================= ENVOYER UNE ALERTE A UN UTILISATEUR =================
    @PostMapping("/send/{userId}")
    public ResponseEntity<?> sendAlertToUser(
            @PathVariable Long userId,
            @RequestBody AlertRequest request,
            Principal principal
    ) {
        try {
            if (principal == null || principal.getName() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Utilisateur non authentifié"));
            }

            if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Le message de l'alerte est obligatoire"));
            }

            User sender = userRepository.findByEmail(principal.getName()).orElse(null);
            if (sender == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Expéditeur introuvable"));
            }

            NotificationDTO created = notificationService.sendManualAlertToUser(
                    userId,
                    request.getMessage().trim(),
                    sender.getId()
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Alerte envoyée avec succès",
                    "notification", created
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur interne lors de l'envoi de l'alerte"));
        }
    }
    
}