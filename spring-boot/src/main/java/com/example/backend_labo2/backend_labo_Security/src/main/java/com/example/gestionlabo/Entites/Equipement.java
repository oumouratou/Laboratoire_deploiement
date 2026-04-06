package com.example.gestionlabo.Entites;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import com.example.gestionlabo.Enums.Etat;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Getter
@Setter
@Entity
@Table(name="equipements")
public class Equipement {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 50)
    private String nom;

    @NotBlank
    @Size(min = 5, max = 200)
    private String caracteristique;

    @Min(1)
    private int quantite;

    @Enumerated(EnumType.STRING)
    private Etat etat = Etat.FONCTIONNEL;

    @ManyToOne
    @JoinColumn(name="laboratoire_id")
    @JsonIgnoreProperties("equipements")
    private Laboratoire laboratoire;

    @OneToMany(mappedBy="equipement", cascade=CascadeType.ALL)
    private List<Reclamation> reclamations;

    @Column(name="date_acquisition")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateAcquisition;

    // Nouveau champ pour l'URL de l'image
    @Column(name="image_url")
    private String imageUrl;
}
