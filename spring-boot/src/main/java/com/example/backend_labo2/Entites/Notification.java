package com.example.backend_labo2.Entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Getter
@Setter
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500)
    private String message;

    @Column(name = "is_read")
    private boolean read = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    private String type; // "RESERVATION" ou "RECLAMATION"

    @Column(length = 500)
    private String motifRefus; // Motif de refus si applicable

    @ManyToOne
    @JoinColumn(name = "technicien_id")
    @JsonIgnoreProperties({"notifications", "password", "reservations", "reclamations"})
    private User technicien;

    @ManyToOne
    @JoinColumn(name = "destinataire_id")
    @JsonIgnoreProperties({"notifications", "password", "reservations", "reclamations"})
    private User destinataire;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    @JsonIgnoreProperties({"notifications", "etudiant", "laboratoire"})
    private Reservation reservation;

    @ManyToOne
    @JoinColumn(name = "reclamation_id")
    @JsonIgnoreProperties({"notifications", "auteur", "laboratoire", "equipement"})
    private Reclamation reclamation;
}