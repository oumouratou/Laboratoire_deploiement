package com.example.backend_labo2.Entites;

import com.example.backend_labo2.Enums.Etat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "reclamations")
public class Reclamation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 5, max = 500)
    private String description;

    @Min(1)
    private int quantite;

    @ManyToOne
    @JoinColumn(name = "laboratoire_id")
    @JsonIgnoreProperties({"equipements", "reservations", "utilisateurs"})
    private Laboratoire laboratoire;

    @ManyToOne
    @JoinColumn(name = "equipement_id")
    @JsonIgnoreProperties("reclamations")
    private Equipement equipement;

    @ManyToOne
    @JoinColumn(name = "auteur_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private User auteur;

    @Enumerated(EnumType.STRING)
    private Etat etat = Etat.NON_TRAITEE;

    private LocalDateTime dateReclamation = LocalDateTime.now();

    @Column
    private String priorite = "MOYENNE";

    @Column
    private String motifRefus;
}