package com.example.backend_labo2.Service;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend_labo2.DTO.ReservationRequest;
import com.example.backend_labo2.DTO.ReservationResponse;
import com.example.backend_labo2.Entites.Laboratoire;
import com.example.backend_labo2.Entites.Reservation;
import com.example.backend_labo2.Entites.User;
import com.example.backend_labo2.Enums.StatutReservation;
import com.example.backend_labo2.Repositories.LaboratoireRepository;
import com.example.backend_labo2.Repositories.ReservationRepository;
import com.example.backend_labo2.Repositories.UserRepository;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LaboratoireRepository laboratoireRepository;

    @Autowired
    private NotificationService notificationService;

    // ================= CREATE =================
    @Transactional
    public Reservation createEntity(ReservationRequest request, Principal principal) {
        User etudiant = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Laboratoire laboratoire = laboratoireRepository.findById(request.getLaboratoireId())
                .orElseThrow(() -> new RuntimeException("Laboratoire introuvable"));

        if (!request.getHeureFin().isAfter(request.getHeureDebut())) {
            throw new RuntimeException("L'heure de fin doit être après l'heure de début");
        }

        Reservation reservation = new Reservation();
        reservation.setEtudiant(etudiant);
        reservation.setLaboratoire(laboratoire);
        reservation.setDateReservation(request.getDateReservation());
        reservation.setHeureDebut(request.getHeureDebut());
        reservation.setHeureFin(request.getHeureFin());
        reservation.setMotif(request.getMotif());
        reservation.setStatut(StatutReservation.EN_ATTENTE);

        Reservation saved = reservationRepository.save(reservation);

        // Notifier le technicien d'une nouvelle réservation
        notificationService.notifyTechnicien(saved);

        return saved;
    }

    // ================= UPDATE =================
    @Transactional
    public Reservation updateEntity(Long id, ReservationRequest request, Principal principal) {
        User etudiant = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation introuvable"));

        if (!reservation.getEtudiant().getId().equals(etudiant.getId())) {
            throw new RuntimeException("Accès refusé : modification non autorisée");
        }

        if (!reservation.getStatut().equals(StatutReservation.EN_ATTENTE)) {
            throw new RuntimeException("Impossible de modifier une réservation déjà traitée");
        }

        Laboratoire laboratoire = laboratoireRepository.findById(request.getLaboratoireId())
                .orElseThrow(() -> new RuntimeException("Laboratoire introuvable"));

        if (!request.getHeureFin().isAfter(request.getHeureDebut())) {
            throw new RuntimeException("L'heure de fin doit être après l'heure de début");
        }

        reservation.setLaboratoire(laboratoire);
        reservation.setDateReservation(request.getDateReservation());
        reservation.setHeureDebut(request.getHeureDebut());
        reservation.setHeureFin(request.getHeureFin());
        reservation.setMotif(request.getMotif());

        return reservationRepository.save(reservation);
    }

    // ================= SAVE =================
    @Transactional
    public Reservation save(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    // ================= FIND =================
    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation introuvable"));
    }

    public Reservation getReservationEntity(Long id) {
        return findById(id);
    }

    // ================= DTO CONVERSION =================
    public ReservationResponse toDto(Reservation r) {
        ReservationResponse dto = new ReservationResponse();

        dto.setId(r.getId());
        dto.setDateReservation(r.getDateReservation());
        dto.setHeureDebut(r.getHeureDebut());
        dto.setHeureFin(r.getHeureFin());
        dto.setMotif(r.getMotif());
        dto.setStatut(r.getStatut() != null ? r.getStatut().name() : null);
        dto.setMotifRefus(r.getMotifRefus());

        // Laboratoire
        if (r.getLaboratoire() != null) {
            dto.setLaboratoireId(r.getLaboratoire().getId());
            dto.setLaboratoireNom(r.getLaboratoire().getNomLabo());
        }

        // Infos complètes du demandeur (étudiant/enseignant)
        if (r.getEtudiant() != null) {
            dto.setEtudiantId(r.getEtudiant().getId());
            dto.setEtudiantNom(r.getEtudiant().getNom());
            dto.setEtudiantPrenom(r.getEtudiant().getPrenom());
            dto.setEtudiantCin(r.getEtudiant().getCin());
            dto.setEtudiantEmail(r.getEtudiant().getEmail());
            dto.setEtudiantRole(r.getEtudiant().getRole() != null
                    ? r.getEtudiant().getRole().name()
                    : null);
        }

        return dto;
    }

    // ================= FIND ALL =================
    public List<Reservation> findAll() {
        return reservationRepository.findAllWithEtudiantAndLaboratoire();
    }

    // ================= FIND BY ETUDIANT =================
    public List<Reservation> findByEtudiant(Long etudiantId) {
        return reservationRepository.findByEtudiantWithLaboratoire(etudiantId);
    }

    // ================= FIND APPROVED BY LABO =================
    public List<Reservation> findApprovedByLaboratoire(Long laboId) {
        return reservationRepository.findByLaboratoireIdAndStatut(laboId, StatutReservation.APPROUVEE);
    }

    public User getUserFromPrincipal(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    // ================= APPROUVER RÉSERVATION =================
    @Transactional
    public ReservationResponse approuverReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));

        reservation.setStatut(StatutReservation.APPROUVEE);
        Reservation saved = reservationRepository.save(reservation);

        // ✅ CORRIGÉ : Utiliser la bonne signature (Reservation, String)
        notificationService.notifyEtudiant(saved, "APPROUVEE");

        return toDto(saved);
    }

    // ================= REFUSER RÉSERVATION =================
    @Transactional
    public ReservationResponse refuserReservation(Long id, String motifRefus) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));

        reservation.setStatut(StatutReservation.REFUSEE);
        reservation.setMotifRefus(motifRefus);
        Reservation saved = reservationRepository.save(reservation);

        // ✅ CORRIGÉ : Utiliser la bonne signature (Reservation, String, String)
        notificationService.notifyEtudiant(saved, "REFUSEE", motifRefus);

        return toDto(saved);
    }

    // ================= ANNULER RÉSERVATION =================
    @Transactional
    public ReservationResponse annulerReservation(Long id, Principal principal) {
        User user = getUserFromPrincipal(principal);
        Reservation reservation = findById(id);

        if (!reservation.getEtudiant().getId().equals(user.getId())) {
            throw new RuntimeException("Accès refusé : vous ne pouvez annuler que vos réservations");
        }

        if (!reservation.getStatut().equals(StatutReservation.EN_ATTENTE)) {
            throw new RuntimeException("Impossible d'annuler une réservation déjà traitée");
        }

        reservation.setStatut(StatutReservation.ANNULEE);
        return toDto(save(reservation));
    }

    // ================= DELETE =================
    @Transactional
    public void delete(Long id, Principal principal) {
        User etudiant = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation introuvable"));

        if (!reservation.getEtudiant().getId().equals(etudiant.getId())) {
            throw new RuntimeException("Accès refusé : vous ne pouvez supprimer que vos réservations");
        }

        if (!reservation.getStatut().equals(StatutReservation.EN_ATTENTE)) {
            throw new RuntimeException("Impossible de supprimer une réservation déjà traitée");
        }

        reservationRepository.delete(reservation);
    }

    // ================= DELETE BY LABO =================
    @Transactional
    public void deleteByLaboratoireId(Long laboId) {
        List<Reservation> reservations = reservationRepository.findByLaboratoireId(laboId);
        reservationRepository.deleteAll(reservations);
    }
}