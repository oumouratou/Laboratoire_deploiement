package com.example.gestionlabo.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.gestionlabo.Entites.Equipement;
import com.example.gestionlabo.Enums.Etat;
import com.example.gestionlabo.Repositories.EquipementRepository;

@Service
public class EquipementService {

    @Autowired
    private EquipementRepository equipementRepository;

    // CREATE
    public Equipement create(Equipement e) {
        // imageUrl est automatiquement pris en compte
        return equipementRepository.save(e);
    }

    // READ ALL
    public List<Equipement> findAll() {
        return equipementRepository.findAll();
    }

    // READ BY ID
    public Equipement findById(Long id) {
        return equipementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipement introuvable"));
    }

    // UPDATE
    public Equipement update(Long id, Equipement newData) {
        Equipement e = findById(id);
        e.setNom(newData.getNom());
        e.setCaracteristique(newData.getCaracteristique());
        e.setQuantite(newData.getQuantite());
        e.setEtat(newData.getEtat());
        e.setLaboratoire(newData.getLaboratoire());
        e.setImageUrl(newData.getImageUrl()); // <-- ajout de l'image
        return equipementRepository.save(e);
    }

    // UPDATE ETAT
    public Equipement updateEtat(Long id, Etat etat) {
        Equipement e = findById(id);
        e.setEtat(etat);
        return equipementRepository.save(e);
    }

    // DELETE
    public void delete(Long id) {
        equipementRepository.deleteById(id);
    }
}
