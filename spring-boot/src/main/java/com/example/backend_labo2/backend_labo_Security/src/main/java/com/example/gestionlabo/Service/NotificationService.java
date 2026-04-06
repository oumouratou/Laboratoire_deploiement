package com.example.gestionlabo.Service;

import com.example.gestionlabo.Entites.Notification;
import com.example.gestionlabo.Entites.User;
import com.example.gestionlabo.Repositories.NotificationRepository;
import com.example.gestionlabo.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }

    public List<Notification> findByUserId(Long userId) {
        return notificationRepository.findByDestinataireIdOrderByCreatedAtDesc(userId);
    }

    public Notification create(Notification notification) {
        return notificationRepository.save(notification);
    }

    public Notification sendToUser(Long userId, String message, String type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        Notification notif = new Notification();
        notif.setDestinataire(user);
        notif.setMessage(message);
        notif.setType(type);
        return notificationRepository.save(notif);
    }

    public Notification markAsRead(Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification introuvable"));
        n.setRead(true);
        return notificationRepository.save(n);
    }

    public void markAllAsRead(Long userId) {
        List<Notification> notifs = notificationRepository.findByDestinataireId(userId);
        for (Notification n : notifs) {
            n.setRead(true);
        }
        notificationRepository.saveAll(notifs);
    }

    public void delete(Long id) {
        notificationRepository.deleteById(id);
    }
}
