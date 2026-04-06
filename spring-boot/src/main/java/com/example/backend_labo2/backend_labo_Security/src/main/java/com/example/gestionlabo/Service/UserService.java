package com.example.gestionlabo.Service;

import com.example.gestionlabo.DTO.*;
import com.example.gestionlabo.Entites.User;
import com.example.gestionlabo.Entites.Departement;
import com.example.gestionlabo.Enums.Role;
import com.example.gestionlabo.Repositories.DepartementRepository;
import com.example.gestionlabo.Repositories.LaboratoireRepository;
import com.example.gestionlabo.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private DepartementRepository departementRepository;
    @Autowired private LaboratoireRepository laboratoireRepository;

    // --------- CREATE ---------
    public User createEtudiant(EtudiantCreateRequest dto) {
        User u = new User();
        u.setNom(dto.getNom());
        u.setPrenom(dto.getPrenom());
        u.setEmail(dto.getEmail());
        u.setPassword(dto.getPassword());
        u.setCin(dto.getCin());
        u.setRole(Role.ETUDIANT);
        u.setActive(true);
        u.setDepartement(
            departementRepository.findById(dto.getDepartementId())
                .orElseThrow(() -> new RuntimeException("Departement introuvable"))
        );
        return userRepository.save(u);
    }

    public User createEnseignant(EnseignantCreateRequest dto) {
        User u = new User();
        u.setNom(dto.getNom());
        u.setPrenom(dto.getPrenom());
        u.setEmail(dto.getEmail());
        u.setPassword(dto.getPassword());
        u.setCin(dto.getCin());
        u.setRole(Role.ENSEIGNANT);
        u.setActive(true);
        u.setDepartement(
            departementRepository.findById(dto.getDepartementId())
                .orElseThrow(() -> new RuntimeException("Departement introuvable"))
        );
        u.setCreatedAt(LocalDateTime.now());
        return userRepository.save(u);
    }

    public User createTechnicien(TechnicienCreateRequest dto) {
        User u = new User();
        u.setNom(dto.getNom());
        u.setPrenom(dto.getPrenom());
        u.setEmail(dto.getEmail());
        u.setPassword(dto.getPassword());
        u.setCin(dto.getCin());
        u.setRole(Role.TECHNICIEN);
        u.setActive(true);
        u.setLaboratoire(
            laboratoireRepository.findById(dto.getLaboratoireId())
                .orElseThrow(() -> new RuntimeException("Laboratoire introuvable"))
        );
        return userRepository.save(u);
    }

    // --------- READ ---------
    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }

    // --------- UPDATE ---------
    public User updateEtudiant(Long id, EtudiantUpdateRequest dto) {
        User u = findById(id);
        u.setNom(dto.getNom());
        u.setPrenom(dto.getPrenom());
        u.setEmail(dto.getEmail());
        u.setCin(dto.getCin());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            u.setPassword(dto.getPassword());
        }
        u.setRole(Role.ETUDIANT);
        u.setDepartement(
            departementRepository.findById(dto.getDepartementId())
                .orElseThrow(() -> new RuntimeException("Departement introuvable"))
        );
        return userRepository.save(u);
    }

    public User updateEnseignant(Long id, EnseignantUpdateRequest dto) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("Enseignant non trouve avec l'id : " + id);
        }
        User enseignant = optionalUser.get();
        enseignant.setNom(dto.getNom());
        enseignant.setPrenom(dto.getPrenom());
        enseignant.setEmail(dto.getEmail());
        enseignant.setCin(dto.getCin());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            enseignant.setPassword(dto.getPassword());
        }
        if (dto.getDepartementId() != null) {
            Departement dep = departementRepository.findById(dto.getDepartementId())
                    .orElseThrow(() -> new RuntimeException("Departement non trouve avec l'id : " + dto.getDepartementId()));
            enseignant.setDepartement(dep);
        }
        return userRepository.save(enseignant);
    }

    // --------- BLOCK / UNBLOCK ---------
    public User blockUser(Long id) {
        User u = findById(id);
        u.setActive(false);
        return userRepository.save(u);
    }

    public User unblockUser(Long id) {
        User u = findById(id);
        u.setActive(true);
        return userRepository.save(u);
    }

    // --------- DELETE ---------
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
