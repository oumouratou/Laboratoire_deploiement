package com.example.backend_labo2.Controller;

import com.example.backend_labo2.Repositories.*;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.backend_labo2.DTO.ReservationRequest;
import com.example.backend_labo2.DTO.ReservationResponse;
import com.example.backend_labo2.Entites.*;
import com.example.backend_labo2.Enums.StatutReservation;
import com.example.backend_labo2.Service.NotificationService;
import com.example.backend_labo2.Service.ReservationService;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "*")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LaboratoireRepository laboratoireRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    // ================= CREATE =================
    @PostMapping
    public ReservationResponse create(@RequestBody ReservationRequest request, Principal principal) {
        Reservation reservation = reservationService.createEntity(request, principal);
        ReservationResponse response = reservationService.toDto(reservation);

        // Notifier le chef de département (plus les techniciens)
        try {
            notificationService.notifyTechnicien(reservation);
        } catch (Exception e) {
            System.err.println("Erreur notification: " + e.getMessage());
        }

        return response;
    }

    // ================= READ ALL =================
    @GetMapping
    public List<ReservationResponse> findAll() {
        return reservationService.findAll()
                .stream()
                .map(reservationService::toDto)
                .toList();
    }

    // ================= READ ALL (alias /all) =================
    @GetMapping("/all")
    public List<ReservationResponse> findAllAlias() {
        return reservationService.findAll()
                .stream()
                .map(reservationService::toDto)
                .toList();
    }

    // ================= READ BY ID =================
    @GetMapping("/{id}")
    public ReservationResponse findById(@PathVariable Long id) {
        return reservationService.toDto(reservationService.findById(id));
    }

    // ================= READ BY ETUDIANT =================
    @GetMapping("/etudiant/{etudiantId}")
    public List<ReservationResponse> findByEtudiant(@PathVariable Long etudiantId) {
        return reservationService.findByEtudiant(etudiantId)
                .stream()
                .map(reservationService::toDto)
                .toList();
    }

    // ================= RÉSERVATIONS DE L'ÉTUDIANT CONNECTÉ =================
    @GetMapping("/mes")
    public List<ReservationResponse> getMesReservations(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return reservationService.findByEtudiant(user.getId())
                .stream()
                .map(reservationService::toDto)
                .toList();
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    public ReservationResponse update(@PathVariable Long id,
                                      @RequestBody ReservationRequest request,
                                      Principal principal) {
        Reservation reservation = reservationService.updateEntity(id, request, principal);
        return reservationService.toDto(reservation);
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Principal principal) {
        reservationService.delete(id, principal);
    }

    // ================= ANNULER =================
    @PutMapping("/{id}/annuler")
    public ReservationResponse annulerReservation(@PathVariable Long id, Principal principal) {
        Reservation reservation = reservationService.getReservationEntity(id);
        User etudiant = reservationService.getUserFromPrincipal(principal);

        if (!reservation.getEtudiant().getId().equals(etudiant.getId())) {
            throw new RuntimeException("Accès refusé : vous ne pouvez annuler que vos réservations");
        }

        if (!reservation.getStatut().equals(StatutReservation.EN_ATTENTE)) {
            throw new RuntimeException("Impossible d'annuler une réservation déjà traitée");
        }

        reservation.setStatut(StatutReservation.ANNULEE);
        return reservationService.toDto(reservationService.save(reservation));
    }

    // ================= AUTO-ANNULER (par l'étudiant, sans notification) =================
    @PutMapping("/{id}/auto-annuler")
    public ReservationResponse autoAnnulerReservation(@PathVariable Long id, Principal principal) {
        Reservation reservation = reservationService.getReservationEntity(id);
        User etudiant = reservationService.getUserFromPrincipal(principal);

        if (!reservation.getEtudiant().getId().equals(etudiant.getId())) {
            throw new RuntimeException("Accès refusé");
        }

        if (!reservation.getStatut().equals(StatutReservation.EN_ATTENTE)) {
            throw new RuntimeException("Impossible d'annuler une réservation déjà traitée");
        }

        reservation.setStatut(StatutReservation.ANNULEE);
        return reservationService.toDto(reservationService.save(reservation));
    }

    // ================= MODIFIER =================
    @PutMapping("/{id}/modifier")
    public ReservationResponse modifierReservation(@PathVariable Long id,
                                                   @RequestBody ReservationRequest request,
                                                   Principal principal) {
        Reservation reservation = reservationService.getReservationEntity(id);
        User etudiant = reservationService.getUserFromPrincipal(principal);

        if (!reservation.getEtudiant().getId().equals(etudiant.getId())) {
            throw new RuntimeException("Accès refusé : vous ne pouvez modifier que vos réservations");
        }

        if (!reservation.getStatut().equals(StatutReservation.EN_ATTENTE)) {
            throw new RuntimeException("Impossible de modifier une réservation déjà traitée");
        }

        if (!request.getHeureFin().isAfter(request.getHeureDebut())) {
            throw new RuntimeException("L'heure de fin doit être après l'heure de début");
        }

        reservation.setDateReservation(request.getDateReservation());
        reservation.setHeureDebut(request.getHeureDebut());
        reservation.setHeureFin(request.getHeureFin());
        reservation.setMotif(request.getMotif());

        return reservationService.toDto(reservationService.save(reservation));
    }

    // ================= APPROUVER (par le chef de département) =================
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveReservation(@PathVariable Long id, Principal principal) {
        try {
            Reservation reservation = reservationService.findById(id);

            if (!reservation.getStatut().equals(StatutReservation.EN_ATTENTE)) {
                return ResponseEntity.badRequest().body("La réservation a déjà été traitée");
            }

            reservation.setStatut(StatutReservation.APPROUVEE);
            Reservation saved = reservationService.save(reservation);
            ReservationResponse response = reservationService.toDto(saved);

            // Notifier l'étudiant uniquement
            try {
                notificationService.notifyEtudiant(saved, "APPROUVEE");
            } catch (Exception e) {
                System.err.println("Erreur notification étudiant: " + e.getMessage());
            }

            // ✅ PAS de notifyOtherTechniciensReservation

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ================= REFUSER (par le chef de département) =================
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectReservation(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            Principal principal) {
        try {
            Reservation reservation = reservationService.findById(id);

            if (!reservation.getStatut().equals(StatutReservation.EN_ATTENTE)) {
                return ResponseEntity.badRequest().body("La réservation a déjà été traitée");
            }

            String motifRefus = (body != null) ? body.get("motifRefus") : null;

            reservation.setStatut(StatutReservation.REFUSEE);
            if (motifRefus != null && !motifRefus.isEmpty()) {
                reservation.setMotifRefus(motifRefus);
            }

            Reservation saved = reservationService.save(reservation);
            ReservationResponse response = reservationService.toDto(saved);

            // Notifier l'étudiant uniquement
            try {
                notificationService.notifyEtudiant(saved, "REFUSEE", motifRefus);
            } catch (Exception e) {
                System.err.println("Erreur notification étudiant: " + e.getMessage());
            }

            // ✅ PAS de notifyOtherTechniciensReservation

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ================= TRAITER AVEC BODY JSON =================
    @PutMapping("/{id}/traiter")
    public ResponseEntity<?> traiterReservation(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Principal principal) {
        try {
            String action = body.get("action");
            String motifRefus = body.get("motifRefus");

            if (action == null || action.isEmpty()) {
                return ResponseEntity.badRequest().body("Action requise (APPROUVEE ou REFUSEE)");
            }

            Reservation reservation = reservationService.findById(id);

            if (!reservation.getStatut().equals(StatutReservation.EN_ATTENTE)) {
                return ResponseEntity.badRequest().body("La réservation a déjà été traitée");
            }

            if ("REFUSEE".equalsIgnoreCase(action) && (motifRefus == null || motifRefus.trim().isEmpty())) {
                return ResponseEntity.badRequest().body("Le motif de refus est obligatoire");
            }

            if ("APPROUVEE".equalsIgnoreCase(action)) {
                reservation.setStatut(StatutReservation.APPROUVEE);
            } else if ("REFUSEE".equalsIgnoreCase(action)) {
                reservation.setStatut(StatutReservation.REFUSEE);
                if (motifRefus != null && !motifRefus.isEmpty()) {
                    reservation.setMotifRefus(motifRefus);
                }
            } else {
                return ResponseEntity.badRequest().body("Action invalide. Utilisez APPROUVEE ou REFUSEE");
            }

            Reservation saved = reservationService.save(reservation);
            ReservationResponse response = reservationService.toDto(saved);

            // Notifier l'étudiant uniquement
            try {
                notificationService.notifyEtudiant(saved, action, motifRefus);
            } catch (Exception e) {
                System.err.println("Erreur notification étudiant: " + e.getMessage());
            }

            // ✅ PAS de notifyOtherTechniciensReservation

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ================= RÉSERVATIONS DU DÉPARTEMENT DU CHEF =================
    @GetMapping("/mon-departement")
    public ResponseEntity<?> getReservationsMonDepartement(Principal principal) {
        try {
            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (!user.isChefDepartement() || user.getDepartement() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Accès réservé aux chefs de département"));
            }

            List<Laboratoire> labos = laboratoireRepository.findByDepartementId(user.getDepartement().getId());
            List<Long> laboIds = labos.stream().map(Laboratoire::getId).toList();

            // ✅ Retourner des DTOs au lieu d'entités brutes (évite les erreurs de sérialisation)
            List<ReservationResponse> filtered = reservationRepository.findAll().stream()
                    .filter(r -> r.getLaboratoire() != null && laboIds.contains(r.getLaboratoire().getId()))
                    .map(reservationService::toDto)
                    .toList();

            return ResponseEntity.ok(filtered);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}