package com.example.backend_labo2.Entites;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import com.example.backend_labo2.Enums.Etat;
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

    @NotBlank(message = "L'identifiant est obligatoire")
    @Size(min = 3, max = 20, message = "L'identifiant doit contenir entre 3 et 20 caractères")
    @Column(unique = true, nullable = false)
    private String identifiant;

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
    @JsonIgnoreProperties({"equipements", "utilisateurs", "reservations"})
    private Laboratoire laboratoire;

    @OneToMany(mappedBy="equipement", cascade=CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"equipement", "laboratoire", "auteur", "enseignant"})
    private List<Reclamation> reclamations;

    @Column(name="date_acquisition")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateAcquisition;
    @Lob
    @Column(name="image_url", columnDefinition = "LONGTEXT")
    private String imageUrl;
    // Génération automatique de l'identifiant si vide
    @PrePersist
    public void generateIdentifiantIfEmpty() {
        if (this.identifiant == null || this.identifiant.isEmpty()) {
            String dateStr = LocalDate.now().toString().replace("-", "");
            String randomStr = generateRandomString(4);
            this.identifiant = "EQ-" + dateStr + "-" + randomStr;
        }
    }

    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
}