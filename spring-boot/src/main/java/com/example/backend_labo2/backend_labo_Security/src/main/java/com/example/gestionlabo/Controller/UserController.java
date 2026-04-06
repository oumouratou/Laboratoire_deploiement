package com.example.gestionlabo.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import com.example.gestionlabo.Config.JwtUtil;
import com.example.gestionlabo.Entites.User;
import com.example.gestionlabo.Service.UserService;
import com.example.gestionlabo.Enums.Role;
import com.example.gestionlabo.Repositories.UserRepository;
import com.example.gestionlabo.DTO.EtudiantCreateRequest;
import com.example.gestionlabo.DTO.EtudiantUpdateRequest;
import com.example.gestionlabo.DTO.EnseignantCreateRequest;
import com.example.gestionlabo.DTO.EnseignantUpdateRequest;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {

    @Autowired private UserService userService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserRepository userRepository;

    // --------- PROFIL (via JWT) ---------
    @GetMapping("/profil")
    public ResponseEntity<?> getProfil(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = jwtUtil.extractEmail(token);
        Optional<User> optUser = userRepository.findByEmail(email);
        if (optUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(optUser.get());
    }

    // --------- ETUDIANTS ---------
    @PostMapping("/etudiants")
    public User createEtudiant(@RequestBody EtudiantCreateRequest dto) {
        return userService.createEtudiant(dto);
    }

    @PutMapping("/etudiants/{id}")
    public User updateEtudiant(@PathVariable Long id, @RequestBody EtudiantUpdateRequest dto) {
        return userService.updateEtudiant(id, dto);
    }

    @GetMapping("/etudiants")
    public List<User> getEtudiants() {
        return userService.findByRole(Role.ETUDIANT);
    }

    @PutMapping("/etudiants/{id}/block")
    public ResponseEntity<User> blockEtudiant(@PathVariable Long id) {
        return ResponseEntity.ok(userService.blockUser(id));
    }

    @PutMapping("/etudiants/{id}/unblock")
    public ResponseEntity<User> unblockEtudiant(@PathVariable Long id) {
        return ResponseEntity.ok(userService.unblockUser(id));
    }

    // --------- ENSEIGNANTS ---------
    @PostMapping("/enseignants")
    public ResponseEntity<User> createEnseignant(@RequestBody EnseignantCreateRequest dto) {
        return ResponseEntity.ok(userService.createEnseignant(dto));
    }

    @PutMapping("/enseignants/{id}")
    public User updateEnseignant(@PathVariable Long id, @RequestBody EnseignantUpdateRequest dto) {
        return userService.updateEnseignant(id, dto);
    }

    @GetMapping("/enseignants")
    public List<User> getEnseignants() {
        return userService.findByRole(Role.ENSEIGNANT);
    }

    @DeleteMapping("/enseignants/{id}")
    public void deleteEnseignant(@PathVariable Long id) {
        userService.delete(id);
    }

    // --------- GENERAL ---------
    @GetMapping
    public List<User> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}
