package com.example.gestionlabo.Entites;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import com.example.gestionlabo.Enums.StatutReservation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 Etudiant
    @ManyToOne
    @JoinColumn(name = "etudiant_id", nullable = false)
    @NotNull
    private User etudiant;

    // 🔗 Laboratoire
    @ManyToOne
    @JoinColumn(name = "laboratoire_id", nullable = false)
    @NotNull
    private Laboratoire laboratoire;

    @Column(name = "date_reservation", nullable = false)
    private LocalDate dateReservation;

    @Column(name = "heure_debut", nullable = false)
    private LocalTime heureDebut;

    @Column(name = "heure_fin", nullable = false)
    private LocalTime heureFin;

    @Column(length = 500)
    private String motif;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutReservation statut;
}
