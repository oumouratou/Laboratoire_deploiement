

package com.example.backend_labo2.DTO;

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
private String password;
private String cin;
private Long departementId;
private Boolean isChefDepartement;
}
