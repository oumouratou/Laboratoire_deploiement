package com.example.gestionlabo.Service;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.gestionlabo.Entites.Departement;
import com.example.gestionlabo.Repositories.DepartementRepository;
@Service
public class DepartementService {
	@Autowired
    private DepartementRepository departementRepository;

    // CREATE
    public Departement create(Departement d) {
        return departementRepository.save(d);
    }

    // READ ALL
    public List<Departement> findAll() {
        return departementRepository.findAll();
    }

    // READ BY ID
    public Departement findById(Long id) {
        return departementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Département introuvable"));
    }

    // UPDATE
 // UPDATE
    public Departement update(Long id, Departement newData) {
        Departement d = findById(id); // récupère l'entité existante
        d.setNom(newData.getNom());
        d.setDescription(newData.getDescription()); // ajoute cette ligne !
        return departementRepository.save(d);
    }

    // DELETE
    public void delete(Long id) {
        departementRepository.deleteById(id);

}
}