

package com.example.backend_labo2.DTO;

import com.example.backend_labo2.Enums.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
private String nom;
private String prenom;
private String email;
private String password;
private String cin;

private Role role;
private Long departementId;

private Boolean isChefDepartement;

// Spécifique étudiant
private String niveau;
private String classe;
}
