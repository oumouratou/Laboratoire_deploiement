
package com.example.backend_labo2.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
private String token;
private String type = "Bearer";
private String nom;
private String prenom;
private String email;
private String role;
}