package com.example.backend_labo2.Entites;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.backend_labo2.Enums.StatutReservation;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etudiant_id")
    private User etudiant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laboratoire_id")
    private Laboratoire laboratoire;

    private LocalDate dateReservation;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String motif;

    @Enumerated(EnumType.STRING)
    private StatutReservation statut;

    // Champ pour le motif de refus
    @Column(length = 500)
    private String motifRefus;
}