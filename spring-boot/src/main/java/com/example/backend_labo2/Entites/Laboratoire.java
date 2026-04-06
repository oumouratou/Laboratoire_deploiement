package com.example.backend_labo2.Entites;

import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.example.backend_labo2.Enums.Etat;

@Getter
@Setter
@Entity
@Table(name="laboratoires")
public class Laboratoire {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du laboratoire est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom du laboratoire doit être entre 2 et 50 caractères")
    @Column(name="nom_labo")
    private String nomLabo;

    @NotNull(message = "L'état du laboratoire doit être renseigné")
    @Enumerated(EnumType.STRING)
    @Column(name="etat_labo")
    private Etat etatLabo = Etat.ACTIF;

    @NotNull(message = "Le département doit être renseigné")
    @ManyToOne
    @JoinColumn(name="department_id")
    @JsonIgnoreProperties({"laboratoires"})
    private Departement departement;

    @OneToMany(mappedBy="laboratoire")
    @JsonIgnoreProperties("laboratoire") // ignore référence inverse
    private List<Equipement> equipements;

    @OneToMany(mappedBy="laboratoire", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<User> utilisateurs;

    @OneToMany(mappedBy="laboratoire", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Reservation> reservations;
}
