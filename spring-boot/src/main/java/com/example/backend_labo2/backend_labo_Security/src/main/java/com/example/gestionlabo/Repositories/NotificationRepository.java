package com.example.gestionlabo.Repositories;

import com.example.gestionlabo.Entites.Notification;
import com.example.gestionlabo.Entites.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByDestinataire(User destinataire);
    List<Notification> findByDestinataireIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByDestinataireId(Long userId);
}
