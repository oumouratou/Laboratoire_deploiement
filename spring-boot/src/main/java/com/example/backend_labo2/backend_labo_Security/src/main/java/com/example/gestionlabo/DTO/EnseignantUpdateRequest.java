package com.example.gestionlabo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnseignantUpdateRequest {
    
    private String nom;
    private String prenom;
    private String email;
    private String password; // optionnel : si vide, le mot de passe ne change pas
    private String cin;
    private Long departementId;
}
