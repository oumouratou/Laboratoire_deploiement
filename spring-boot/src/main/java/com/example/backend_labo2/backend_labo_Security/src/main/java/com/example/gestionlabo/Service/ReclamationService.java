package com.example.gestionlabo.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.gestionlabo.Entites.Reclamation;
import com.example.gestionlabo.Enums.Etat;
import com.example.gestionlabo.Repositories.ReclamationRepository;

@Service
public class ReclamationService {

    @Autowired
    private ReclamationRepository reclamationRepository;

    public Reclamation create(Reclamation r) {
        r.setEtat(Etat.NON_TRAITEE);
        return reclamationRepository.save(r);
    }

    public List<Reclamation> findAll() {
        return reclamationRepository.findAll();
    }

    public Reclamation findById(Long id) {
        return reclamationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reclamation introuvable"));
    }

    public List<Reclamation> findByAuteurId(Long auteurId) {
        return reclamationRepository.findByAuteurId(auteurId);
    }

    public Reclamation update(Long id, Reclamation newData) {
        Reclamation r = findById(id);
        r.setDescription(newData.getDescription());
        r.setQuantite(newData.getQuantite());
        if (newData.getEquipement() != null) r.setEquipement(newData.getEquipement());
        if (newData.getEtat() != null) r.setEtat(newData.getEtat());
        return reclamationRepository.save(r);
    }

    public Reclamation traiter(Long id) {
        Reclamation r = findById(id);
        r.setEtat(Etat.TRAITEE);
        return reclamationRepository.save(r);
    }

    public Reclamation traiterAvecAction(Long id, String action, String motifRefus) {
        Reclamation r = findById(id);
        r.setEtat(Etat.valueOf(action));
        return reclamationRepository.save(r);
    }

    public Reclamation autoAnnuler(Long id) {
        Reclamation r = findById(id);
        r.setEtat(Etat.ANNULEE);
        return reclamationRepository.save(r);
    }

    public void delete(Long id) {
        reclamationRepository.deleteById(id);
    }
}
