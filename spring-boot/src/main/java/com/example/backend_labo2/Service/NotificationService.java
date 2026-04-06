package com.example.backend_labo2.Service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.example.backend_labo2.Repositories.NotificationRepository;
import com.example.backend_labo2.Repositories.UserRepository;
import com.example.backend_labo2.Entites.Notification;
import com.example.backend_labo2.Entites.User;
import com.example.backend_labo2.Entites.Reclamation;
import com.example.backend_labo2.Entites.Reservation;
import com.example.backend_labo2.DTO.NotificationDTO;
import com.example.backend_labo2.Enums.Role;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // ================= NOTIFICATIONS RECLAMATION → TECHNICIEN + ENSEIGNANT =================
    public void notifyTechnicienReclamation(Reclamation reclamation) {
        if (reclamation == null || reclamation.getAuteur() == null) return;

        // Vérifier si une notification existe déjà pour cette réclamation (anti-doublon)
        List<Notification> existingNotifs = notificationRepository.findByReclamation(reclamation);
        if (!existingNotifs.isEmpty()) {
            System.out.println("Notification déjà existante pour la réclamation " + reclamation.getId());
            return;
        }

        String equipName = (reclamation.getEquipement() != null) ? reclamation.getEquipement().getNom() : "un équipement";
        String auteurNom = reclamation.getAuteur().getPrenom() + " " + reclamation.getAuteur().getNom();
        String auteurRole = reclamation.getAuteur().getRole() == Role.ETUDIANT ? "l'étudiant" : "l'enseignant";

        // ✅ 1) Notifier TOUS les techniciens
        List<User> techniciens = userRepository.findByRole(Role.TECHNICIEN);
        for (User technicien : techniciens) {
            Notification notif = new Notification();
            notif.setTechnicien(technicien);
            notif.setReclamation(reclamation);
            notif.setType("RECLAMATION");
            notif.setMessage("Nouvelle réclamation de " + auteurRole + " " + auteurNom + " pour " + equipName);
            notif.setCreatedAt(LocalDateTime.now());
            notif.setRead(false);

            notificationRepository.save(notif);
            messagingTemplate.convertAndSend("/topic/technicien/" + technicien.getId(), toDto(notif));
        }

        // ✅ 2) Notifier les ENSEIGNANTS du même département (si la réclamation vient d'un étudiant)
        if (reclamation.getAuteur().getRole() == Role.ETUDIANT 
                && reclamation.getAuteur().getDepartement() != null) {
            Long deptId = reclamation.getAuteur().getDepartement().getId();
            List<User> enseignants = userRepository.findByRole(Role.ENSEIGNANT);
            
            for (User enseignant : enseignants) {
                // Notifier seulement les enseignants du même département
                if (enseignant.getDepartement() != null 
                        && enseignant.getDepartement().getId().equals(deptId)) {
                    Notification notif = new Notification();
                    notif.setDestinataire(enseignant);
                    notif.setReclamation(reclamation);
                    notif.setType("RECLAMATION");
                    notif.setMessage("Nouvelle réclamation de l'étudiant " + auteurNom + " pour " + equipName);
                    notif.setCreatedAt(LocalDateTime.now());
                    notif.setRead(false);

                    notificationRepository.save(notif);
                    messagingTemplate.convertAndSend("/topic/enseignant/" + enseignant.getId(), toDto(notif));
                }
            }
        }
    }

    // ================= NOTIFICATIONS RECLAMATION → AUTEUR (ETUDIANT/ENSEIGNANT) =================
    public void notifyUtilisateurReclamation(Reclamation reclamation, String statut) {
        notifyUtilisateurReclamation(reclamation, statut, null);
    }

    public void notifyUtilisateurReclamation(Reclamation reclamation, String statut, String motifRefus) {
        if (reclamation == null || reclamation.getAuteur() == null) return;

        User auteur = reclamation.getAuteur();
        
        // ✅ Anti-doublon : vérifier s'il y a déjà une notification de RÉPONSE pour cette réclamation
        List<Notification> existingNotifs = notificationRepository.findByReclamation(reclamation);
        boolean dejaNotifie = existingNotifs.stream()
                .anyMatch(n -> n.getDestinataire() != null 
                        && n.getDestinataire().getId().equals(auteur.getId())
                        && n.getMessage() != null
                        && (n.getMessage().contains("a été traitée") 
                            || n.getMessage().contains("a été refusée")
                            || n.getMessage().contains("a été mise à jour")));
        
        if (dejaNotifie) {
            System.out.println("Notification de réponse déjà envoyée pour la réclamation " + reclamation.getId());
            return;
        }

        Notification notif = new Notification();
        notif.setDestinataire(auteur);
        notif.setReclamation(reclamation);
        notif.setType("RECLAMATION");
        notif.setCreatedAt(LocalDateTime.now());
        notif.setRead(false);

        String equipName = (reclamation.getEquipement() != null) ? reclamation.getEquipement().getNom() : "un équipement";

        switch (statut.toUpperCase()) {
            case "TRAITEE" -> notif.setMessage("✅ Votre réclamation pour " + equipName + " a été traitée !");
            case "REFUSEE", "ANNULEE" -> {
                notif.setMessage("❌ Votre réclamation pour " + equipName + " a été refusée.");
                if (motifRefus != null && !motifRefus.isEmpty()) {
                    notif.setMotifRefus(motifRefus);
                }
            }
            default -> notif.setMessage("Votre réclamation pour " + equipName + " a été mise à jour.");
        }

        notificationRepository.save(notif);

        String topic;
        if (auteur.getRole() == Role.ETUDIANT) {
            topic = "/topic/etudiant/";
        } else if (auteur.getRole() == Role.ENSEIGNANT) {
            topic = "/topic/enseignant/";
        } else {
            topic = "/topic/user/";
        }

        messagingTemplate.convertAndSend(topic + auteur.getId(), toDto(notif));
    }

 

    // ================= NOTIFICATIONS RESERVATION → ETUDIANT =================
    public void notifyEtudiant(Reservation reservation, String statut) {
        notifyEtudiant(reservation, statut, null);
    }

   

    // ================= NOTIFIER LES AUTRES TECHNICIENS =================
    public void notifyOtherTechniciens(Long treatingTechnicienId, String action, String entityType, String entityDetails) {
        List<User> techniciens = userRepository.findByRole(Role.TECHNICIEN);

        for (User technicien : techniciens) {
            if (technicien.getId().equals(treatingTechnicienId)) {
                continue;
            }

            Notification notif = new Notification();
            notif.setTechnicien(technicien);
            notif.setType("INFO");
            notif.setMessage("Un collègue a " + action + " " + entityType + " : " + entityDetails);
            notif.setCreatedAt(LocalDateTime.now());
            notif.setRead(false);

            notificationRepository.save(notif);
            messagingTemplate.convertAndSend("/topic/technicien/" + technicien.getId(), toDto(notif));
        }
    }

    public void notifyOtherTechniciensReclamation(Long treatingTechnicienId, Reclamation reclamation, String statut) {
        String action = statut.equalsIgnoreCase("TRAITEE") ? "traité" : "refusé";
        String equipName = reclamation.getEquipement() != null ? reclamation.getEquipement().getNom() : "un équipement";
        notifyOtherTechniciens(treatingTechnicienId, action, "la réclamation pour", equipName);
    }

    public void notifyOtherTechniciensReservation(Long treatingTechnicienId, Reservation reservation, String statut) {
        String action = statut.equalsIgnoreCase("APPROUVEE") ? "approuvé" : "refusé";
        String laboName = reservation.getLaboratoire() != null ? reservation.getLaboratoire().getNomLabo() : "un laboratoire";
        notifyOtherTechniciens(treatingTechnicienId, action, "la réservation du labo", laboName);
    }

    // ================= RECUPERATION DES NOTIFICATIONS =================
    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        List<Notification> notificationsTech = notificationRepository.findByTechnicienAndReadFalse(user);
        List<Notification> notificationsDest = notificationRepository.findByDestinataireAndReadFalse(user);

        List<Notification> allNotifications = new ArrayList<>();
        allNotifications.addAll(notificationsTech);
        allNotifications.addAll(notificationsDest);

        allNotifications.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        return allNotifications.stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<NotificationDTO> getAllNotificationsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        List<Notification> notificationsTech = notificationRepository.findByTechnicienOrderByCreatedAtDesc(user);
        List<Notification> notificationsDest = notificationRepository.findByDestinataireOrderByCreatedAtDesc(user);

        List<Notification> allNotifications = new ArrayList<>();
        allNotifications.addAll(notificationsTech);
        allNotifications.addAll(notificationsDest);

        allNotifications.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        return allNotifications.stream().map(this::toDto).collect(Collectors.toList());
    }

    // ================= CONVERSION DTO =================
    private NotificationDTO toDto(Notification n) {
        return NotificationDTO.fromEntity(n);
    }

    // ================= MARQUER COMME LU =================
    public void markAsRead(Long notificationId) {
        Notification notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification introuvable"));
        notif.setRead(true);
        notificationRepository.save(notif);
    }

    public void markAllAsRead(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        List<Notification> unreadTech = notificationRepository.findByTechnicienAndReadFalse(user);
        List<Notification> unreadDest = notificationRepository.findByDestinataireAndReadFalse(user);

        unreadTech.forEach(n -> n.setRead(true));
        unreadDest.forEach(n -> n.setRead(true));

        notificationRepository.saveAll(unreadTech);
        notificationRepository.saveAll(unreadDest);
    }

    public List<NotificationDTO> getAllNotificationsForUser(Long userId) {
        List<Notification> notifications = notificationRepository
            .findByDestinataire_IdOrTechnicien_IdOrderByCreatedAtDesc(userId, userId);

        return notifications.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public void markAllAsReadForUser(Long userId) {
        List<Notification> notifications = new ArrayList<>();
        notifications.addAll(notificationRepository.findByDestinataire_IdAndReadFalse(userId));
        notifications.addAll(notificationRepository.findByTechnicien_IdAndReadFalse(userId));

        for (Notification n : notifications) {
            n.setRead(true);
        }
        notificationRepository.saveAll(notifications);
    }
 // ================= NOTIFICATIONS RESERVATION → TECHNICIEN + CHEF DÉPARTEMENT =================
 // ================= NOTIFICATIONS RESERVATION → CHEF DE DÉPARTEMENT UNIQUEMENT =================
    public void notifyTechnicien(Reservation reservation) {
        if (reservation == null || reservation.getLaboratoire() == null) return;

        // Anti-doublon
        List<Notification> existingNotifs = notificationRepository.findByReservation(reservation);
        if (!existingNotifs.isEmpty()) {
            System.out.println("Notification déjà existante pour la réservation " + reservation.getId());
            return;
        }

        String laboNom = reservation.getLaboratoire().getNomLabo();
        String etudiantNom = reservation.getEtudiant().getPrenom() + " " + reservation.getEtudiant().getNom();

        // ✅ PAS de notification aux techniciens pour les réservations

        // ✅ Notifier uniquement le CHEF DE DÉPARTEMENT du labo réservé
        if (reservation.getLaboratoire().getDepartement() != null) {
            Long deptId = reservation.getLaboratoire().getDepartement().getId();
            List<User> enseignants = userRepository.findByRole(Role.ENSEIGNANT);
            
            for (User enseignant : enseignants) {
                if (enseignant.isChefDepartement() 
                        && enseignant.getDepartement() != null 
                        && enseignant.getDepartement().getId().equals(deptId)) {
                    
                    Notification notif = new Notification();
                    notif.setDestinataire(enseignant);
                    notif.setReservation(reservation);
                    notif.setType("RESERVATION");
                    notif.setMessage("Nouvelle réservation de " + etudiantNom + " pour le labo " + laboNom);
                    notif.setCreatedAt(LocalDateTime.now());
                    notif.setRead(false);

                    notificationRepository.save(notif);
                    messagingTemplate.convertAndSend("/topic/enseignant/" + enseignant.getId(), toDto(notif));
                }
            }
        }
    }
    
    public void notifyEtudiant(Reservation reservation, String statut, String motifRefus) {
        if (reservation == null || reservation.getEtudiant() == null) return;

        User etudiant = reservation.getEtudiant();
        
        // Anti-doublon
        List<Notification> existingNotifs = notificationRepository.findByReservation(reservation);
        boolean dejaNotifie = existingNotifs.stream()
                .anyMatch(n -> n.getDestinataire() != null
                        && n.getDestinataire().getId().equals(etudiant.getId())
                        && n.getMessage() != null
                        && (n.getMessage().contains("a été approuvée")
                            || n.getMessage().contains("a été refusée")));
        
        if (dejaNotifie) {
            System.out.println("Notification déjà envoyée pour la réservation " + reservation.getId());
            return;
        }

        Notification notif = new Notification();
        notif.setDestinataire(etudiant);
        notif.setReservation(reservation);
        notif.setType("RESERVATION");
        notif.setCreatedAt(LocalDateTime.now());
        notif.setRead(false);

        String laboNom = reservation.getLaboratoire() != null
                ? reservation.getLaboratoire().getNomLabo()
                : "le laboratoire";

        switch (statut.toUpperCase()) {
            case "APPROUVEE" -> notif.setMessage("✅ Votre réservation du labo " + laboNom + " a été approuvée !");
            case "REFUSEE" -> {
                notif.setMessage("❌ Votre réservation du labo " + laboNom + " a été refusée.");
                if (motifRefus != null && !motifRefus.isEmpty()) {
                    notif.setMotifRefus(motifRefus);
                }
            }
            default -> notif.setMessage("Mise à jour de votre réservation du labo " + laboNom);
        }

        notificationRepository.save(notif);
        messagingTemplate.convertAndSend("/topic/etudiant/" + etudiant.getId(), toDto(notif));

        // ✅ PAS de notification au chef de département ici
        // (C'est le chef lui-même qui traite, il n'a pas besoin d'être notifié de sa propre action)
    }
    
    public NotificationDTO sendManualAlertToUser(Long destinataireId, String message, Long senderId) {
        User destinataire = userRepository.findById(destinataireId)
                .orElseThrow(() -> new RuntimeException("Destinataire introuvable"));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Expéditeur introuvable"));

        Notification notif = new Notification();
        notif.setDestinataire(destinataire);

        // Si l'expéditeur est technicien, on garde la relation technicien pour cohérence métier
        if (sender.getRole() == Role.TECHNICIEN) {
            notif.setTechnicien(sender);
        }

        notif.setType("ALERT");
        notif.setMessage(message);
        notif.setCreatedAt(LocalDateTime.now());
        notif.setRead(false);

        Notification saved = notificationRepository.save(notif);

        // Push websocket selon rôle destinataire
        String topic;
        if (destinataire.getRole() == Role.ETUDIANT) {
            topic = "/topic/etudiant/";
        } else if (destinataire.getRole() == Role.ENSEIGNANT) {
            topic = "/topic/enseignant/";
        } else if (destinataire.getRole() == Role.TECHNICIEN) {
            topic = "/topic/technicien/";
        } else {
            topic = "/topic/user/";
        }

        messagingTemplate.convertAndSend(topic + destinataire.getId(), toDto(saved));
        return toDto(saved);
    }
    }