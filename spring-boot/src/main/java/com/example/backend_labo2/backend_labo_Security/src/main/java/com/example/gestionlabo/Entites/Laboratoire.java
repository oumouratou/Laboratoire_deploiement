package com.example.gestionlabo.Entites;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*; // pour les validations
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import com.example.gestionlabo.Enums.Etat;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity
@Table(name="laboratoires")
@Getter
@Setter
public class Laboratoire {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    // Nom obligatoire, 2 à 50 caractères
    @NotBlank(message = "Le nom du laboratoire est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom du laboratoire doit être entre 2 et 50 caractères")
    @Column(name="nom_labo")
    private String nomLabo;

    // Etat obligatoire
    @NotNull(message = "L'état du laboratoire doit être renseigné")
    @Enumerated(EnumType.STRING)
    @Column(name="etat_labo")
    private Etat etatLabo = Etat.ACTIF;

    // Département obligatoire
    @NotNull(message = "Le département doit être renseigné")
    @ManyToOne
    @JoinColumn(name="department_id")
    private Departement departement;

    // Liste des équipements liés à ce laboratoire
    @OneToMany(mappedBy="laboratoire", cascade = CascadeType.ALL)
    private List<Equipement> equipements;


    // Liste des utilisateurs
    @OneToMany(mappedBy="laboratoire")
    private List<User> utilisateurs;

    // Liste des réservations
    @OneToMany(mappedBy="laboratoire")
    @JsonIgnore
    private List<Reservation> reservations;
}
