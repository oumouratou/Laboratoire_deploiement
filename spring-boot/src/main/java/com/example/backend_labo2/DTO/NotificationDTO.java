package com.example.backend_labo2.DTO;

import com.example.backend_labo2.Entites.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
    private Long reservationId;
    private Long reclamationId;
    private String laboName;
    private String type; // "RESERVATION", "RECLAMATION", "INFO"
    private String demandeurNom;
    private String demandeurPrenom;
    private String demandeurCin;
    private String motifRefus;
    private String equipementNom;

    // Constructeur simplifié pour compatibilité
    public NotificationDTO(Long id, String message, boolean read, LocalDateTime createdAt,
                           Long reservationId, String laboName, String type) {
        this.id = id;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
        this.reservationId = reservationId;
        this.laboName = laboName;
        this.type = type;
    }

    // Méthode statique pour convertir Notification -> DTO
    public static NotificationDTO fromEntity(Notification n) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(n.getId());
        dto.setMessage(n.getMessage());
        dto.setRead(n.isRead());
        dto.setCreatedAt(n.getCreatedAt());
        dto.setMotifRefus(n.getMotifRefus());
        dto.setType(n.getType() != null ? n.getType() : "INFO");

        if (n.getReservation() != null) {
            dto.setType("RESERVATION");
            dto.setReservationId(n.getReservation().getId());
            if (n.getReservation().getLaboratoire() != null) {
                dto.setLaboName(n.getReservation().getLaboratoire().getNomLabo());
            }
            if (n.getReservation().getEtudiant() != null) {
                dto.setDemandeurNom(n.getReservation().getEtudiant().getNom());
                dto.setDemandeurPrenom(n.getReservation().getEtudiant().getPrenom());
                dto.setDemandeurCin(n.getReservation().getEtudiant().getCin());
            }
        } else if (n.getReclamation() != null) {
            dto.setType("RECLAMATION");
            dto.setReclamationId(n.getReclamation().getId());
            if (n.getReclamation().getLaboratoire() != null) {
                dto.setLaboName(n.getReclamation().getLaboratoire().getNomLabo());
            }
            if (n.getReclamation().getEquipement() != null) {
                dto.setEquipementNom(n.getReclamation().getEquipement().getNom());
            }
            if (n.getReclamation().getAuteur() != null) {
                dto.setDemandeurNom(n.getReclamation().getAuteur().getNom());
                dto.setDemandeurPrenom(n.getReclamation().getAuteur().getPrenom());
                dto.setDemandeurCin(n.getReclamation().getAuteur().getCin());
            }
        }

        return dto;
    }
}