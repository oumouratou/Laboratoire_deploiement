package com.example.backend_labo2.DTO;

import com.example.backend_labo2.Enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthReponse {
private String token;

@Builder.Default
private String type = "Bearer";

private Long id;
private String email;
private String nom;
private String prenom;
private Role role;

private String cin;
private LocalDateTime dateCreation;

private Long departementId;
private String departementNom;

@JsonProperty("isChefDepartement")
private boolean chefDepartement;

private String niveau;
private String classe;

private String attestationUrl;
private Boolean attestationVerifiee;
}




















