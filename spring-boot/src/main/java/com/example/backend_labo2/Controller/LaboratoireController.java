package com.example.backend_labo2.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.backend_labo2.Entites.Laboratoire;
import com.example.backend_labo2.Service.LaboratoireService;
import com.example.backend_labo2.Enums.Etat;
import java.security.Principal;
@RestController
@RequestMapping("/api/laboratoires")
@CrossOrigin(origins = "http://localhost:5173")
public class LaboratoireController {

    @Autowired
    private LaboratoireService laboratoireService;

    // CREATE
    @PostMapping
    public Laboratoire create(@RequestBody Laboratoire labo) {
        return laboratoireService.create(labo);
    }

    // READ ALL
    @GetMapping
    public List<Laboratoire> findAll() {
        return laboratoireService.findAll();
    }

    // READ BY ID
    @GetMapping("/{id}")
    public Laboratoire findById(@PathVariable Long id) {
        return laboratoireService.findById(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public Laboratoire update(@PathVariable Long id, @RequestBody Laboratoire labo) {
        return laboratoireService.update(id, labo);
    }

    // DELETE avec gestion d'erreur
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        try {
            laboratoireService.delete(id);
            return ResponseEntity.ok("Laboratoire supprimé avec succès !");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Enum Etat
    @GetMapping("/etats")
    public Etat[] getEtats() {
        return Etat.values();
    }
    
    //LABOS
    @GetMapping("/mes-labos")
    public ResponseEntity<?> getLabosPourEtudiant(Principal principal) {
    if (principal == null || principal.getName() == null) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Non authentifié");
    }
    return ResponseEntity.ok(laboratoireService.getLabosByEtudiant(principal.getName()));
    }
}
