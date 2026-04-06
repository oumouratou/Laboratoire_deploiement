package com.example.backend_labo2.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend_labo2.Entites.Laboratoire;
import com.example.backend_labo2.Repositories.LaboratoireRepository;
import com.example.backend_labo2.Repositories.EquipementRepository;
import com.example.backend_labo2.Repositories.ReservationRepository;
import com.example.backend_labo2.Repositories.UserRepository;

@Service
public class LaboratoireService {

    @Autowired
    private LaboratoireRepository laboratoireRepository;

    @Autowired
    private EquipementRepository equipementRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

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

    // DELETE sécurisé
    @Transactional
    public void delete(Long id) {
        Laboratoire labo = findById(id);

        // 1️⃣ Supprimer toutes les réservations liées
        reservationRepository.deleteAllByLaboratoireId(id);

        // 2️⃣ Supprimer tous les utilisateurs liés
        userRepository.deleteAllByLaboratoireId(id);

        // 3️⃣ Supprimer tous les équipements liés
        equipementRepository.deleteAllByLaboratoireId(id);

        // 4️⃣ Supprimer le laboratoire lui-même
        laboratoireRepository.delete(labo);
    }
    public List<Laboratoire> getLabosByEtudiant(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .map(user -> {
                    Long deptId = user.getDepartement().getId();
                    return laboratoireRepository.findByDepartementId(deptId);
                })
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }
}
