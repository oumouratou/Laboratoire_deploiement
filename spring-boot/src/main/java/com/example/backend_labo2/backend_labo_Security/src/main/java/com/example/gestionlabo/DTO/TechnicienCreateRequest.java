package com.example.gestionlabo.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter

public class TechnicienCreateRequest {

	
	    private String nom;
	    private String prenom;
	    private String email;
	    private String password;
	    private String cin;
	    private Long laboratoireId;
	}

