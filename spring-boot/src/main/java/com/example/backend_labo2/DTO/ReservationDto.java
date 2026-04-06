package com.example.backend_labo2.DTO;

import com.example.backend_labo2.Entites.Reservation;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
public class ReservationDto {
    
    private Long id;
    private LocalDate dateReservation;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String statut;
    private String motif;
    
    // Infos du laboratoire
    private Long laboratoireId;
    private String laboratoireNom;
    
    // Infos du demandeur (étudiant ou enseignant)
    private Long etudiantId;
    private String etudiantNom;
    private String etudiantPrenom;
    private String etudiantCin;
    private String etudiantRole;
    private String etudiantEmail;
    
    // Constructeur depuis l'entité Reservation
    public ReservationDto(Reservation reservation) {
        this.id = reservation.getId();
        this.dateReservation = reservation.getDateReservation();
        this.heureDebut = reservation.getHeureDebut();
        this.heureFin = reservation.getHeureFin();
        this.statut = reservation.getStatut() != null ? reservation.getStatut().name() : null;
        this.motif = reservation.getMotif();
        
        // Laboratoire
        if (reservation.getLaboratoire() != null) {
            this.laboratoireId = reservation.getLaboratoire().getId();
            this.laboratoireNom = reservation.getLaboratoire().getNomLabo();
        }
        
        // Demandeur (étudiant)
        if (reservation.getEtudiant() != null) {
            this.etudiantId = reservation.getEtudiant().getId();
            this.etudiantNom = reservation.getEtudiant().getNom();
            this.etudiantPrenom = reservation.getEtudiant().getPrenom(); // ✅ AJOUTÉ
            this.etudiantCin = reservation.getEtudiant().getCin();
            this.etudiantEmail = reservation.getEtudiant().getEmail();
            this.etudiantRole = reservation.getEtudiant().getRole() != null 
                ? reservation.getEtudiant().getRole().name() 
                : null;
        }
    }
    
    public static ReservationDto fromEntity(Reservation reservation) {
        return new ReservationDto(reservation);
    }
}