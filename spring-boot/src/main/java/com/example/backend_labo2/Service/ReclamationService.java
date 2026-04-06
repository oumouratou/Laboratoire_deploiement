package com.example.backend_labo2.Service;

import com.example.backend_labo2.DTO.ReclamationDto;
import com.example.backend_labo2.Entites.*;
import com.example.backend_labo2.Enums.Etat;
import com.example.backend_labo2.Enums.Role;
import com.example.backend_labo2.Repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReclamationService {

    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private ReclamationRepository reclamationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LaboratoireRepository laboratoireRepository;
    
    @Autowired
    private EquipementRepository equipementRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // ================= RECUPERATION DES RECLAMATIONS =================

    public List<ReclamationDto> getMesReclamations(String email) {
        return reclamationRepository.findByAuteurEmail(email)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<ReclamationDto> getReclamationsPourTechnicien(String email) {
        return reclamationRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<ReclamationDto> getAllReclamations() {
        return reclamationRepository.findAllByOrderByDateReclamationDesc()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<ReclamationDto> getReclamationsEtudiantConnecte(String email) {
        return reclamationRepository.findByAuteurEmailOrderByDateReclamationDesc(email)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<ReclamationDto> getReclamationsTechnicienConnecte(String email) {
        User tech = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Technicien non trouvé"));

        Laboratoire labo = tech.getLaboratoire();
        if (labo == null) return List.of();

        return reclamationRepository.findByLaboratoireWithDetails(labo)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<ReclamationDto> getReclamationsByEnseignant(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        return reclamationRepository.findByAuteurOrderByDateReclamationDesc(user)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<Equipement> getEquipementsByLabo(Long laboId) {
        Laboratoire labo = laboratoireRepository.findById(laboId)
                .orElseThrow(() -> new RuntimeException("Laboratoire introuvable"));
        return equipementRepository.findByLaboratoire(labo);
    }

    // ================= CREATION =================

   
 // Remplace UNIQUEMENT la méthode createReclamation dans ReclamationService
    @Transactional
    public ReclamationDto createReclamation(ReclamationDto dto, String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Utilisateur non authentifié");
        }
        if (dto == null) {
            throw new RuntimeException("Payload réclamation invalide");
        }
        if (dto.getDescription() == null || dto.getDescription().trim().length() < 5) {
            throw new RuntimeException("Description obligatoire (min 5 caractères)");
        }

        User auteur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + email));

        if (auteur.getId() == null || !userRepository.existsById(auteur.getId())) {
            throw new RuntimeException("Auteur invalide en base (id introuvable)");
        }

        Reclamation r = new Reclamation();
        r.setDescription(dto.getDescription().trim());
        r.setQuantite(dto.getQuantite() != null && dto.getQuantite() > 0 ? dto.getQuantite() : 1);
        r.setPriorite(dto.getPriorite() != null && !dto.getPriorite().isBlank() ? dto.getPriorite() : "MOYENNE");
        r.setAuteur(auteur);
        r.setEtat(Etat.NON_TRAITEE);
        r.setDateReclamation(LocalDateTime.now());

        Laboratoire labo = null;
        if (dto.getLaboratoireId() != null) {
            labo = laboratoireRepository.findById(dto.getLaboratoireId())
                    .orElseThrow(() -> new RuntimeException("Laboratoire introuvable"));
            r.setLaboratoire(labo);
        }

        if (dto.getEquipementId() != null) {
            Equipement eq = equipementRepository.findById(dto.getEquipementId())
                    .orElseThrow(() -> new RuntimeException("Équipement introuvable"));

            if (labo != null && eq.getLaboratoire() != null
                    && !eq.getLaboratoire().getId().equals(labo.getId())) {
                throw new RuntimeException("L'équipement ne correspond pas au laboratoire sélectionné");
            }

            r.setEquipement(eq);

            if (labo == null && eq.getLaboratoire() != null) {
                r.setLaboratoire(eq.getLaboratoire());
            }
        }

        Reclamation saved = reclamationRepository.save(r);
        notifyAllTechniciens(saved, auteur);
        return mapToDto(saved);
    }

    // ================= TRAITEMENT =================

    @Transactional
    public ReclamationDto traiterReclamation(Long id, String action, String emailTechnicien) {
        return traiterReclamation(id, action, emailTechnicien, null);
    }

    @Transactional
    public ReclamationDto traiterReclamation(Long id, String action, String emailTechnicien, String motifRefus) {
        User tech = userRepository.findByEmail(emailTechnicien)
                .orElseThrow(() -> new RuntimeException("Technicien non trouvé"));

        Reclamation rec = reclamationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réclamation introuvable"));

        if (rec.getEtat() != Etat.NON_TRAITEE) {
            throw new RuntimeException("Cette réclamation a déjà été traitée");
        }

        String message;
        String statut;

        if (action.equalsIgnoreCase("APPROUVER") || action.equalsIgnoreCase("TRAITEE")) {
            rec.setEtat(Etat.TRAITEE);
            message = "✅ Votre réclamation a été approuvée et traitée.";
            statut = "TRAITEE";
        } else if (action.equalsIgnoreCase("REFUSER") || action.equalsIgnoreCase("REFUSEE")) {
            rec.setEtat(Etat.ANNULEE);
            if (motifRefus == null || motifRefus.trim().isEmpty()) {
                throw new RuntimeException("Le motif de refus est obligatoire");
            }
            rec.setMotifRefus(motifRefus);
            message = "❌ Votre réclamation a été refusée.";
            statut = "REFUSEE";
        } else {
            throw new RuntimeException("Action invalide. Utilisez APPROUVER/TRAITEE ou REFUSER/REFUSEE");
        }

        reclamationRepository.save(rec);
        notifyAuteur(rec, message, motifRefus);
        notifyOtherTechniciens(tech.getId(), rec, statut);

        return mapToDto(rec);
    }

    // ================= MODIFICATION =================

    @Transactional
    public ReclamationDto updateReclamation(Long id, ReclamationDto dto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Reclamation rec = reclamationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réclamation introuvable"));

        if (!rec.getAuteur().getId().equals(user.getId())) {
            throw new RuntimeException("Vous ne pouvez modifier que vos propres réclamations");
        }

        if (rec.getEtat() != Etat.NON_TRAITEE) {
            throw new RuntimeException("Cette réclamation a déjà été traitée et ne peut plus être modifiée");
        }

        if (dto.getDescription() != null) {
            rec.setDescription(dto.getDescription());
        }
        if (dto.getQuantite() != null) {
            rec.setQuantite(dto.getQuantite());
        }
        if (dto.getPriorite() != null) {
            rec.setPriorite(dto.getPriorite());
        }

        reclamationRepository.save(rec);
        return mapToDto(rec);
    }

    // ================= AUTO-ANNULATION =================

    @Transactional
    public ReclamationDto autoAnnulerReclamation(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Reclamation rec = reclamationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réclamation introuvable"));

        if (!rec.getAuteur().getId().equals(user.getId())) {
            throw new RuntimeException("Vous ne pouvez annuler que vos propres réclamations");
        }

        if (rec.getEtat() != Etat.NON_TRAITEE) {
            throw new RuntimeException("Cette réclamation a déjà été traitée");
        }

        rec.setEtat(Etat.ANNULEE);
        reclamationRepository.save(rec);

        return mapToDto(rec);
    }

    // ================= NOTIFICATIONS =================

    

    

    private void notifyOtherTechniciens(Long treatingTechnicienId, Reclamation reclamation, String statut) {
        String action = statut.equalsIgnoreCase("TRAITEE") ? "traité" : "refusé";
        String equipName = reclamation.getEquipement() != null ? reclamation.getEquipement().getNom() : "un équipement";
        String message = "Un collègue a " + action + " la réclamation pour " + equipName;

        List<User> techniciens = userRepository.findByRole(Role.TECHNICIEN);

        for (User tech : techniciens) {
            if (tech.getId().equals(treatingTechnicienId)) {
                continue;
            }

            Notification notif = new Notification();
            notif.setMessage(message);
            notif.setTechnicien(tech);
            notif.setReclamation(reclamation);
            notif.setType("INFO");
            notif.setCreatedAt(LocalDateTime.now());
            notif.setRead(false);
            notificationRepository.save(notif);

            messagingTemplate.convertAndSend("/topic/technicien/" + tech.getId(), notif);
        }
    }

    private String getTopicForUser(User user) {
        if (user.getRole() == Role.ENSEIGNANT) {
            return "/topic/enseignant/" + user.getId();
        } else if (user.getRole() == Role.ETUDIANT) {
            return "/topic/etudiant/" + user.getId();
        }
        return "/topic/user/" + user.getId();
    }

    // ================= MAPPING DTO =================

    public ReclamationDto mapToDto(Reclamation rec) {
        ReclamationDto dto = new ReclamationDto();

        dto.setId(rec.getId());
        dto.setDescription(rec.getDescription());
        dto.setQuantite(rec.getQuantite());
        dto.setPriorite(rec.getPriorite());
        dto.setEtat(rec.getEtat() != null ? rec.getEtat().name() : null);
        dto.setDateReclamation(rec.getDateReclamation());
        dto.setMotifRefus(rec.getMotifRefus());

        if (rec.getLaboratoire() != null) {
            dto.setLaboratoireId(rec.getLaboratoire().getId());
            dto.setLaboratoireNom(rec.getLaboratoire().getNomLabo());
        }

        if (rec.getEquipement() != null) {
            dto.setEquipementId(rec.getEquipement().getId());
            dto.setEquipementNom(rec.getEquipement().getNom());
        }

        if (rec.getAuteur() != null) {
            dto.setNomAuteur(rec.getAuteur().getNom());
            dto.setPrenomAuteur(rec.getAuteur().getPrenom());
            dto.setRoleAuteur(rec.getAuteur().getRole().name());
            dto.setAuteurId(rec.getAuteur().getId());
            dto.setCinAuteur(rec.getAuteur().getCin());  // ✅ CIN ajouté
        }

        return dto;
    }
    
    
    
 // ================= NOTIFICATIONS =================

 // Notifier tous les techniciens d'une nouvelle réclamation
 private void notifyAllTechniciens(Reclamation reclamation, User auteur) {
     // Vérifier si une notification existe déjà pour cette réclamation (pour les techniciens)
     List<Notification> existingNotifs = notificationRepository.findByReclamation(reclamation);
     boolean techNotifExists = existingNotifs.stream()
             .anyMatch(n -> n.getTechnicien() != null);
     
     if (techNotifExists) {
         System.out.println("Notification technicien déjà existante pour la réclamation " + reclamation.getId());
         return;
     }

     String roleAuteur = auteur.getRole().name();
     String equipName = reclamation.getEquipement() != null ? reclamation.getEquipement().getNom() : "un équipement";
     String message = "Nouvelle réclamation de " + auteur.getPrenom() + " " + auteur.getNom()
             + " (" + roleAuteur + ") pour " + equipName;

     List<User> techniciens = userRepository.findByRole(Role.TECHNICIEN);

     for (User tech : techniciens) {
         Notification notif = new Notification();
         notif.setMessage(message);
         notif.setTechnicien(tech);
         notif.setReclamation(reclamation);
         notif.setType("RECLAMATION");
         notif.setCreatedAt(LocalDateTime.now());
         notif.setRead(false);
         notificationRepository.save(notif);

         messagingTemplate.convertAndSend("/topic/technicien/" + tech.getId(), notif);
     }
 }

 // Notifier l'auteur de la réclamation (étudiant ou enseignant) - AVEC VÉRIFICATION ANTI-DOUBLON
 private void notifyAuteur(Reclamation rec, String message, String motifRefus) {
     if (rec.getAuteur() == null) return;

     // ✅ VÉRIFICATION ANTI-DOUBLON : Vérifier si une notification de traitement existe déjà
     List<Notification> existingNotifs = notificationRepository.findByReclamation(rec);
     boolean auteurNotifExists = existingNotifs.stream()
             .anyMatch(n -> n.getDestinataire() != null 
                     && n.getDestinataire().getId().equals(rec.getAuteur().getId())
                     && (n.getMessage().contains("approuvée") || n.getMessage().contains("refusée")));
     
     if (auteurNotifExists) {
         System.out.println("Notification auteur déjà existante pour la réclamation " + rec.getId());
         return;
     }

     Notification notif = new Notification();
     notif.setMessage(message + " (Réclamation #" + rec.getId() + ")");
     notif.setDestinataire(rec.getAuteur());
     notif.setReclamation(rec);
     notif.setType("RECLAMATION");
     notif.setCreatedAt(LocalDateTime.now());
     notif.setRead(false);
     
     if (motifRefus != null && !motifRefus.isEmpty()) {
         notif.setMotifRefus(motifRefus);
     }
     
     notificationRepository.save(notif);

     // Envoyer via WebSocket selon le rôle
     String topic = getTopicForUser(rec.getAuteur());
     messagingTemplate.convertAndSend(topic, notif);
 }
}