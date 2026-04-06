package com.example.backend_labo2.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnseignantRequest {
    private String nom;
    private String prenom;
    private String email;
    private String password; // Optionnel pour la mise à jour
    private String cin;
    private Long departementId;
    private Boolean isChefDepartement;
}
