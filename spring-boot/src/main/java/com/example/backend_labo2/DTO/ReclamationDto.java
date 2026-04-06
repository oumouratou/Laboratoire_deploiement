package com.example.backend_labo2.DTO;

import java.time.LocalDateTime;
import com.example.backend_labo2.Entites.Reclamation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReclamationDto {
    private Long id;
    private String description;
    private String etat;
    private String nomAuteur;
    private String prenomAuteur;
    private String roleAuteur;
    private String cinAuteur;           // ✅ CIN de l'auteur
    private String laboratoireNom;
    private String equipementNom;
    private Integer quantite;
    private Long laboratoireId;
    private Long equipementId; 
    private Long auteurId;
    private LocalDateTime dateReclamation;
    private String priorite;
    private String motifRefus;

    // Constructeur par défaut
    public ReclamationDto() {}

    // Constructeur avec Reclamation
    public ReclamationDto(Reclamation rec) {
        this.id = rec.getId();
        this.description = rec.getDescription();
        this.etat = rec.getEtat() != null ? rec.getEtat().toString() : null;
        this.quantite = rec.getQuantite();
        this.dateReclamation = rec.getDateReclamation();
        this.priorite = rec.getPriorite();
        this.motifRefus = rec.getMotifRefus();
        
        if (rec.getAuteur() != null) {
            this.nomAuteur = rec.getAuteur().getNom();
            this.prenomAuteur = rec.getAuteur().getPrenom();
            this.roleAuteur = rec.getAuteur().getRole().name();
            this.auteurId = rec.getAuteur().getId();
            this.cinAuteur = rec.getAuteur().getCin();  // ✅ CIN
        }
        
        if (rec.getLaboratoire() != null) {
            this.laboratoireId = rec.getLaboratoire().getId();
            this.laboratoireNom = rec.getLaboratoire().getNomLabo();
        }
        
        if (rec.getEquipement() != null) {
            this.equipementId = rec.getEquipement().getId();
            this.equipementNom = rec.getEquipement().getNom();
        }
    }
}