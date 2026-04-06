package com.example.backend_labo2.Service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend_labo2.Entites.Equipement;
import com.example.backend_labo2.Enums.Etat;
import com.example.backend_labo2.Repositories.EquipementRepository;
import com.example.backend_labo2.Repositories.ReclamationRepository;

import jakarta.transaction.Transactional;

@Service
public class EquipementService {

    @Autowired
    private EquipementRepository equipementRepository;
    
    @Autowired
    private ReclamationRepository reclamationRepository;

    // ================= CREATE =================
    public Equipement create(Equipement e) {
        // Vérifier doublon d'identifiant
        if (e.getIdentifiant() != null && !e.getIdentifiant().isEmpty()) {
            if (equipementRepository.existsByIdentifiant(e.getIdentifiant())) {
                throw new RuntimeException("Un équipement avec cet identifiant existe déjà: " + e.getIdentifiant());
            }
        }
        
        // Vérifier doublon nom + laboratoire
        if (e.getLaboratoire() != null && equipementRepository.existsByNomAndLaboratoireId(e.getNom(), e.getLaboratoire().getId())) {
            throw new RuntimeException("Un équipement avec ce nom existe déjà dans ce laboratoire");
        }
        
        return equipementRepository.save(e);
    }

    // ================= READ ALL =================
    public List<Equipement> findAll() {
        return equipementRepository.findAll();
    }

    // ================= READ BY ID =================
    public Equipement findById(Long id) {
        return equipementRepository.findById(id).orElse(null);
    }

    // ================= READ BY IDENTIFIANT =================
    public Equipement findByIdentifiant(String identifiant) {
        return equipementRepository.findByIdentifiant(identifiant).orElse(null);
    }

    // ================= UPDATE =================
    @Transactional
    public Equipement update(Long id, Equipement e) {
        Equipement existing = equipementRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Équipement non trouvé avec l'ID: " + id));

        // Vérifier si le nouvel identifiant est unique (si modifié)
        if (e.getIdentifiant() != null && !e.getIdentifiant().equals(existing.getIdentifiant())) {
            if (equipementRepository.existsByIdentifiant(e.getIdentifiant())) {
                throw new RuntimeException("Un équipement avec cet identifiant existe déjà: " + e.getIdentifiant());
            }
            existing.setIdentifiant(e.getIdentifiant());
        }

        existing.setNom(e.getNom());
        existing.setCaracteristique(e.getCaracteristique());
        existing.setQuantite(e.getQuantite());
        existing.setEtat(e.getEtat());
        existing.setDateAcquisition(e.getDateAcquisition());
        existing.setImageUrl(e.getImageUrl());
        
        if (e.getLaboratoire() != null) {
            existing.setLaboratoire(e.getLaboratoire());
        }

        return equipementRepository.save(existing);
    }

    // ================= UPDATE ETAT =================
    public Equipement updateEtat(Long id, Etat etat) {
        Equipement existing = equipementRepository.findById(id).orElse(null);
        if (existing != null) {
            existing.setEtat(etat);
            return equipementRepository.save(existing);
        }
        return null;
    }

    // ================= DELETE =================
    @Transactional
    public void delete(Long id) {
        Equipement equipement = equipementRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Équipement non trouvé avec l'ID: " + id));
        
        // Supprimer d'abord les réclamations liées à cet équipement
        reclamationRepository.deleteByEquipementId(id);
        
        // Ensuite supprimer l'équipement
        equipementRepository.delete(equipement);
    }

    // ================= FIND BY LABO =================
    public List<Equipement> findByLaboratoireId(Long laboId) {
        return equipementRepository.findByLaboratoireId(laboId);
    }

    // ================= CHECK IDENTIFIANT DISPONIBLE =================
    public boolean isIdentifiantAvailable(String identifiant) {
        return !equipementRepository.existsByIdentifiant(identifiant);
    }

    // ================= GENERER IDENTIFIANT UNIQUE =================
    public String generateUniqueIdentifiant() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        String baseId;
        int attempts = 0;

        do {
            String randomStr = generateRandomString(4);
            baseId = "EQ-" + dateStr + "-" + randomStr;
            attempts++;
        } while (equipementRepository.existsByIdentifiant(baseId) && attempts < 100);

        return baseId;
    }

    // ================= RECHERCHE =================
    public List<Equipement> searchByNomOrIdentifiant(String search) {
        return equipementRepository.searchByNomOrIdentifiant(search);
    }

    // ================= HELPER =================
    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
}