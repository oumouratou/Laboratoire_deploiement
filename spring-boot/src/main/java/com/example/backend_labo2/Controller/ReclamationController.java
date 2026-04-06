package com.example.backend_labo2.Controller;

import com.example.backend_labo2.DTO.ReclamationDto;
import com.example.backend_labo2.Entites.Equipement;
import com.example.backend_labo2.Entites.Laboratoire;
import com.example.backend_labo2.Entites.Reclamation;
import com.example.backend_labo2.Entites.User;
import com.example.backend_labo2.Repositories.LaboratoireRepository;
import com.example.backend_labo2.Repositories.ReclamationRepository;
import com.example.backend_labo2.Repositories.UserRepository;
import com.example.backend_labo2.Service.NotificationService;
import com.example.backend_labo2.Service.ReclamationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reclamations")
@CrossOrigin(origins = "*")
public class ReclamationController {

    @Autowired
    private ReclamationService reclamationService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LaboratoireRepository laboratoireRepository;
    
    @Autowired
    private ReclamationRepository reclamationRepository;

    // ================= MES RECLAMATIONS (étudiant / enseignant) =================
    @GetMapping("/mes")
    public List<ReclamationDto> getMesReclamations(Principal principal) {
        return reclamationService.getMesReclamations(principal.getName());
    }

    // ================= RECLAMATIONS POUR TECHNICIEN =================
    @GetMapping("/pour-technicien")
    public List<ReclamationDto> getReclamationsPourTechnicien(Principal principal) {
        return reclamationService.getReclamationsPourTechnicien(principal.getName());
    }

    // ================= TOUTES LES RECLAMATIONS =================
    @GetMapping("/all")
    public List<ReclamationDto> getAllReclamations(Principal principal) {
        return reclamationService.getAllReclamations();
    }

    // ================= RECLAMATIONS ETUDIANT CONNECTE =================
    @GetMapping("/etudiant")
    public List<ReclamationDto> getReclamationsEtudiantConnecte(Principal principal) {
        return reclamationService.getReclamationsEtudiantConnecte(principal.getName());
    }

    // ================= RECLAMATIONS TECHNICIEN CONNECTE =================
    @GetMapping("/technicien")
    public List<ReclamationDto> getReclamationsTechnicienConnecte(Principal principal) {
        return reclamationService.getReclamationsTechnicienConnecte(principal.getName());
    }

    // ================= RECLAMATIONS ENSEIGNANT =================
    @GetMapping("/pour-enseignant")
    public List<ReclamationDto> getReclamationsEnseignant(Principal principal) {
        return reclamationService.getReclamationsByEnseignant(principal.getName());
    }

    // ================= CREATION =================
 // Remplace la méthode createReclamation dans ReclamationController
    @PostMapping("/create")
    public ResponseEntity<?> createReclamation(@RequestBody ReclamationDto dto, Principal principal) {
        try {
            if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
                return ResponseEntity.status(401).body("Utilisateur non authentifié");
            }

            ReclamationDto created = reclamationService.createReclamation(dto, principal.getName());

            try {
                Reclamation reclamation = reclamationRepository.findById(created.getId()).orElse(null);
                if (reclamation != null) {
                    notificationService.notifyTechnicienReclamation(reclamation);
                }
            } catch (Exception e) {
                System.err.println("Erreur notification technicien: " + e.getMessage());
            }

            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur interne: " + e.getMessage());
        }
    }

    // ================= TRAITER (Approuver / Refuser) =================
    @PutMapping("/{id}/traiter")
    public ResponseEntity<?> traiterReclamation(
            @PathVariable Long id,
            @RequestParam String action,
            @RequestParam(required = false) String motifRefus,
            Principal principal) {
        try {
            // Récupérer le technicien qui traite
            User technicien = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Technicien non trouvé"));
            
            // Traiter la réclamation
            ReclamationDto result = reclamationService.traiterReclamation(id, action, principal.getName(), motifRefus);
            
            // Récupérer l'entité pour les notifications
            Reclamation reclamation = reclamationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Réclamation non trouvée"));
            
            // Notifier l'auteur (étudiant ou enseignant)
            try {
                notificationService.notifyUtilisateurReclamation(reclamation, action, motifRefus);
            } catch (Exception e) {
                System.err.println("Erreur notification utilisateur: " + e.getMessage());
            }
            
            // Notifier les autres techniciens
            try {
                notificationService.notifyOtherTechniciensReclamation(technicien.getId(), reclamation, action);
            } catch (Exception e) {
                System.err.println("Erreur notification autres techniciens: " + e.getMessage());
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ================= TRAITER AVEC BODY JSON =================
    @PutMapping("/{id}/traiter-json")
    public ResponseEntity<?> traiterReclamationJson(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Principal principal) {
        try {
            String action = body.get("action");
            String motifRefus = body.get("motifRefus");
            
            if (action == null || action.isEmpty()) {
                return ResponseEntity.badRequest().body("Action requise");
            }
            
            // Récupérer le technicien qui traite
            User technicien = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Technicien non trouvé"));
            
            // Vérifier que le motif est fourni en cas de refus
            if ("REFUSEE".equalsIgnoreCase(action) && (motifRefus == null || motifRefus.trim().isEmpty())) {
                return ResponseEntity.badRequest().body("Le motif de refus est obligatoire");
            }
            
            // Traiter la réclamation
            ReclamationDto result = reclamationService.traiterReclamation(id, action, principal.getName(), motifRefus);
            
            // Récupérer l'entité pour les notifications
            Reclamation reclamation = reclamationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Réclamation non trouvée"));
            
            // Notifier l'auteur (étudiant ou enseignant)
            try {
                notificationService.notifyUtilisateurReclamation(reclamation, action, motifRefus);
            } catch (Exception e) {
                System.err.println("Erreur notification utilisateur: " + e.getMessage());
            }
            
            // Notifier les autres techniciens
            try {
                notificationService.notifyOtherTechniciensReclamation(technicien.getId(), reclamation, action);
            } catch (Exception e) {
                System.err.println("Erreur notification autres techniciens: " + e.getMessage());
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ================= MODIFIER =================
    @PutMapping("/{id}")
    public ResponseEntity<ReclamationDto> updateReclamation(
            @PathVariable Long id,
            @RequestBody ReclamationDto dto,
            Principal principal) {
        return ResponseEntity.ok(reclamationService.updateReclamation(id, dto, principal.getName()));
    }

    // ================= AUTO-ANNULATION =================
    @PutMapping("/{id}/auto-annuler")
    public ReclamationDto autoAnnulerReclamation(@PathVariable Long id, Principal principal) {
        return reclamationService.autoAnnulerReclamation(id, principal.getName());
    }

    // ================= EQUIPEMENTS PAR LABO =================
    @GetMapping("/by-labo/{laboId}")
    public ResponseEntity<List<Equipement>> getEquipementsByLabo(@PathVariable Long laboId) {
        return ResponseEntity.ok(reclamationService.getEquipementsByLabo(laboId));
    }

    // ================= LABOS PAR DEPARTEMENT =================
    @GetMapping("/laboratoires/by-departement")
    public ResponseEntity<List<Laboratoire>> getLaboratoiresByDepartement(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (user.getDepartement() == null) {
            return ResponseEntity.badRequest().body(null);
        }

        return ResponseEntity.ok(
                laboratoireRepository.findByDepartementId(user.getDepartement().getId())
        );
    }
    
   
}