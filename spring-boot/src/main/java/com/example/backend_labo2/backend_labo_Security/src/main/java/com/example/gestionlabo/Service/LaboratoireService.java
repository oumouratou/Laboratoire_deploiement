package com.example.gestionlabo.Service;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.gestionlabo.Entites.Laboratoire;
import com.example.gestionlabo.Repositories.LaboratoireRepository;

@Service
public class LaboratoireService {
	@Autowired
    private LaboratoireRepository laboratoireRepository;

    // CREATE
    public Laboratoire create(Laboratoire labo) {
        return laboratoireRepository.save(labo);
    }

    // READ ALL
    public List<Laboratoire> findAll() {
        return laboratoireRepository.findAll();
    }

    // READ BY ID
    public Laboratoire findById(Long id) {
        return laboratoireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Laboratoire introuvable"));
    }

    // UPDATE
    public Laboratoire update(Long id, Laboratoire newData) {
        Laboratoire labo = findById(id);
        labo.setNomLabo(newData.getNomLabo());
        labo.setDepartement(newData.getDepartement());
        labo.setEtatLabo(newData.getEtatLabo());
        return laboratoireRepository.save(labo);
    }

    // DELETE
    public void delete(Long id) {
        laboratoireRepository.deleteById(id);
    }

}
