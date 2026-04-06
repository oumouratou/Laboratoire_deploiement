package com.example.backend_labo2.DTO;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class EtudiantUpdateRequest {
    private String nom;
    private String prenom;
    private String email;
    private String password; // facultatif
    private String cin;
    private Long departementId;

    // getters et setters
}