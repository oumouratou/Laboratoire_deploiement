package com.example.backend_labo2.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend_labo2.Entites.Notification;
import com.example.backend_labo2.Entites.User;
import com.example.backend_labo2.Entites.Reservation;
import com.example.backend_labo2.Entites.Reclamation;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Pour technicien
    List<Notification> findByTechnicienAndReadFalse(User technicien);
    List<Notification> findByTechnicienOrderByCreatedAtDesc(User technicien);
    
    // Pour destinataire (étudiant/enseignant)
    List<Notification> findByDestinataireAndReadFalse(User destinataire);
    List<Notification> findByDestinataireOrderByCreatedAtDesc(User destinataire);
    
    // Pour vérification des doublons
    List<Notification> findByReservation(Reservation reservation);
    List<Notification> findByReclamation(Reclamation reclamation);
    
    // Optionnel: Recherche par type
    List<Notification> findByTechnicienAndType(User technicien, String type);
    List<Notification> findByDestinataireAndType(User destinataire, String type);
    
 // Toutes les notifications d'un utilisateur (triées par date)
    List<Notification> findByDestinataire_IdOrTechnicien_IdOrderByCreatedAtDesc(Long destinataireId, Long technicienId);

    // Notifications non lues d'un destinataire
    List<Notification> findByDestinataire_IdAndReadFalse(Long destinataireId);

    // Notifications non lues d'un technicien
    List<Notification> findByTechnicien_IdAndReadFalse(Long technicienId);
    
    
}