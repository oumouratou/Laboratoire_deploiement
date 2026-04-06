package com.example.backend_labo2.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.backend_labo2.Entites.Departement;
import com.example.backend_labo2.Service.DepartementService;

@RestController
@RequestMapping("/api/departements")
@CrossOrigin("*")
public class DepartementController {

    @Autowired
    private DepartementService departementService;

    // CREATE
    @PostMapping
    public Departement create(@RequestBody Departement d) {
        return departementService.create(d);
    }

    // READ ALL
    @GetMapping
    public List<Departement> findAll() {
        return departementService.findAll();
    }

    // READ ACTIFS (pour etudiants/enseignants)
    @GetMapping("/actifs")
    public List<Departement> findActifs() {
        return departementService.findActifs();
    }

    // READ BY ID
    @GetMapping("/{id}")
    public Departement findById(@PathVariable Long id) {
        return departementService.findById(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public Departement update(@PathVariable Long id, @RequestBody Departement d) {
        return departementService.update(id, d);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        departementService.delete(id);
    }
}