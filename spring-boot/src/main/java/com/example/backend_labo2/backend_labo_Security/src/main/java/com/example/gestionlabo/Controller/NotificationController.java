package com.example.gestionlabo.Controller;

import com.example.gestionlabo.Config.JwtUtil;
import com.example.gestionlabo.Entites.Notification;
import com.example.gestionlabo.Entites.User;
import com.example.gestionlabo.Enums.Role;
import com.example.gestionlabo.Repositories.UserRepository;
import com.example.gestionlabo.Service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin("*")
public class NotificationController {

    @Autowired private NotificationService notificationService;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtUtil jwtUtil;

    // Helper: extract user from JWT
    private User extractUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) return null;
        String email = jwtUtil.extractEmail(token);
        return userRepository.findByEmail(email).orElse(null);
    }

    // GET /notifications - all notifications (for current user or all)
    @GetMapping
    public ResponseEntity<List<Notification>> getAll(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = extractUser(authHeader);
        if (user != null) {
            return ResponseEntity.ok(notificationService.findByUserId(user.getId()));
        }
        return ResponseEntity.ok(notificationService.findAll());
    }

    // GET /notifications/all - alias
    @GetMapping("/all")
    public ResponseEntity<List<Notification>> getAllAlias(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return getAll(authHeader);
    }

    // GET /notifications/technicien - notifications for technicien(s)
    @GetMapping("/technicien")
    public ResponseEntity<List<Notification>> getForTechnicien(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = extractUser(authHeader);
        if (user != null) {
            return ResponseEntity.ok(notificationService.findByUserId(user.getId()));
        }
        return ResponseEntity.ok(notificationService.findAll());
    }

    // GET /notifications/etudiant - notifications for the current etudiant
    @GetMapping("/etudiant")
    public ResponseEntity<List<Notification>> getForEtudiant(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = extractUser(authHeader);
        if (user != null) {
            return ResponseEntity.ok(notificationService.findByUserId(user.getId()));
        }
        return ResponseEntity.ok(List.of());
    }

    // GET /notifications/etudiant/all
    @GetMapping("/etudiant/all")
    public ResponseEntity<List<Notification>> getForEtudiantAll(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return getForEtudiant(authHeader);
    }

    // GET /notifications/enseignant
    @GetMapping("/enseignant")
    public ResponseEntity<List<Notification>> getForEnseignant(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = extractUser(authHeader);
        if (user != null) {
            return ResponseEntity.ok(notificationService.findByUserId(user.getId()));
        }
        return ResponseEntity.ok(List.of());
    }

    // GET /notifications/enseignant/all
    @GetMapping("/enseignant/all")
    public ResponseEntity<List<Notification>> getForEnseignantAll(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return getForEnseignant(authHeader);
    }

    // POST /notifications - create/send notification
    @PostMapping
    public ResponseEntity<Notification> create(@RequestBody Map<String, Object> body) {
        Object userIdObj = body.get("userId");
        String message = (String) body.get("message");
        String type = body.containsKey("type") ? (String) body.get("type") : "ALERT";

        if (userIdObj == null || message == null) {
            return ResponseEntity.badRequest().build();
        }

        Long userId = Long.valueOf(userIdObj.toString());
        Notification notif = notificationService.sendToUser(userId, message, type);
        return ResponseEntity.ok(notif);
    }

    // POST /notifications/alert/{userId}
    @PostMapping("/alert/{userId}")
    public ResponseEntity<Notification> sendAlert(
            @PathVariable Long userId, @RequestBody Map<String, String> body) {
        String message = body.get("message");
        Notification notif = notificationService.sendToUser(userId, message, "ALERT");
        return ResponseEntity.ok(notif);
    }

    // POST /notifications/send/{userId}
    @PostMapping("/send/{userId}")
    public ResponseEntity<Notification> sendNotification(
            @PathVariable Long userId, @RequestBody Map<String, String> body) {
        String message = body.get("message");
        Notification notif = notificationService.sendToUser(userId, message, "ALERT");
        return ResponseEntity.ok(notif);
    }

    // PUT /notifications/read/{id}
    @PutMapping("/read/{id}")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    // PUT /notifications/read-all
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = extractUser(authHeader);
        if (user != null) {
            notificationService.markAllAsRead(user.getId());
        }
        return ResponseEntity.ok().build();
    }

    // DELETE /notifications/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        notificationService.delete(id);
        return ResponseEntity.ok().build();
    }
}
