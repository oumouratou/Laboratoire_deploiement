package com.example.gestionlabo.Entites;

import jakarta.persistence.*;
import jakarta.validation.constraints.*; // Pour les validations
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import com.example.gestionlabo.Enums.Etat;

@Getter
@Setter
@Entity
@Table(name = "reclamations")
public class Reclamation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Description obligatoire, longueur entre 5 et 500 caractères
    @NotBlank(message = "La description est obligatoire")
    @Size(min = 5, max = 500, message = "La description doit contenir entre 5 et 500 caractères")
    private String description;

    // Quantité positive
    @Min(value = 1, message = "La quantité doit être au moins 1")
    private int quantite;

    // Laboratoire obligatoire
    @NotNull(message = "Le laboratoire doit être renseigné")
    @ManyToOne
    @JoinColumn(name = "laboratoire_id")
    private Laboratoire laboratoire;

    // Équipement obligatoire
    @NotNull(message = "L'équipement doit être renseigné")
    @ManyToOne
    @JoinColumn(name = "equipement_id")
    private Equipement equipement;

    // Auteur obligatoire
    @NotNull(message = "L'auteur de la réclamation doit être renseigné")
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User auteur;

    // Etat obligatoire
    @NotNull(message = "L'état doit être renseigné")
    @Enumerated(EnumType.STRING)
    private Etat etat = Etat.NON_TRAITEE;

    // Date obligatoire, par défaut maintenant
    @NotNull(message = "La date de la réclamation doit être renseignée")
    private LocalDateTime dateReclamation = LocalDateTime.now();
}
