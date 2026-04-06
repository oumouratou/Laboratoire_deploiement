package com.example.backend_labo2.DTO;

import com.example.backend_labo2.Enums.Etat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipementDto {

    private Long id;

    @Size(min = 3, max = 20, message = "L'identifiant doit contenir entre 3 et 20 caractères")
    private String identifiant;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50)
    private String nom;

    @NotBlank(message = "La caractéristique est obligatoire")
    @Size(min = 5, max = 200)
    private String caracteristique;

    @Min(value = 1, message = "La quantité doit être au moins 1")
    private int quantite;

    private Etat etat = Etat.FONCTIONNEL;

    @NotNull(message = "Le laboratoire est obligatoire")
    private Long laboratoireId;

    private String laboratoireNom;

    private LocalDate dateAcquisition;

    private String imageUrl;

    private int nombreReclamations;

    private boolean hasReclamationEnCours;
}