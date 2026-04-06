package com.example.backend_labo2.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {

    private Long id;
    private LocalDate dateReservation;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String motif;
    private String statut;
    private String motifRefus;

    // Laboratoire
    private Long laboratoireId;
    private String laboratoireNom;

    // Étudiant/Demandeur
    private Long etudiantId;
    private String etudiantNom;
    private String etudiantPrenom;
    private String etudiantCin;
    private String etudiantEmail;
    private String etudiantRole;
}