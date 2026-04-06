package com.example.backend_labo2.DTO;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class EnseignantCreateRequest {
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String cin;
    private Long departementId;
    private Boolean isChefDepartement;
    
}