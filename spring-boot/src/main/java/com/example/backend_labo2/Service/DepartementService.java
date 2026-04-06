package com.example.backend_labo2.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend_labo2.Entites.Departement;
import com.example.backend_labo2.Entites.Laboratoire;
import com.example.backend_labo2.Repositories.DepartementRepository;
import com.example.backend_labo2.Repositories.EquipementRepository;
import com.example.backend_labo2.Repositories.LaboratoireRepository;
import com.example.backend_labo2.Repositories.ReservationRepository;
import com.example.backend_labo2.Repositories.UserRepository;

@Service
public class DepartementService {

    @Autowired
    private DepartementRepository departementRepository;

    @Autowired
    private LaboratoireRepository laboratoireRepository;

    @Autowired
    private EquipementRepository equipementRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    // CREATE
    public Departement create(Departement d) {
        if (d.getActif() == null) {
            d.setActif(true);
        }
        return departementRepository.save(d);
    }

    // READ ALL
    public List<Departement> findAll() {
        return departementRepository.findAll();
    }

    // READ ACTIFS
    public List<Departement> findActifs() {
        return departementRepository.findByActifTrue();
    }

    // READ BY ID
    public Departement findById(Long id) {
        return departementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Département introuvable"));
    }

    // UPDATE
    public Departement update(Long id, Departement newData) {
        Departement d = findById(id);

        d.setNom(newData.getNom());
        d.setDescription(newData.getDescription());

        // Met à jour l'état si fourni (actif/inactif)
        if (newData.getActif() != null) {
            d.setActif(newData.getActif());
        }

        return departementRepository.save(d);
    }

    // DELETE avec cascade complète
    @Transactional
    public void delete(Long id) {
        // 0) Détacher les utilisateurs rattachés
        userRepository.detachByDepartementId(id);

        // 1) Récupérer tous les laboratoires du département
        List<Laboratoire> labos = laboratoireRepository.findByDepartementId(id);

        // 2) Supprimer toutes les réservations de chaque labo
        for (Laboratoire l : labos) {
            reservationRepository.deleteByLaboratoireId(l.getId());
        }

        // 3) Supprimer tous les équipements de chaque labo
        for (Laboratoire l : labos) {
            equipementRepository.deleteByLaboratoireId(l.getId());
        }

        // 4) Supprimer tous les laboratoires du département
        laboratoireRepository.deleteByDepartementId(id);

        // 5) Supprimer le département
        departementRepository.deleteById(id);
    }
}