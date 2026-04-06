package com.example.backend_labo2.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.backend_labo2.Entites.Equipement;
import com.example.backend_labo2.Enums.Etat;
import com.example.backend_labo2.Service.EquipementService;

@RestController
@RequestMapping("/api/equipements")
@CrossOrigin("*")
public class EquipementController {

    @Autowired
    private EquipementService equipementService;

    @PostMapping
    public Equipement create(@RequestBody Equipement e) {
        return equipementService.create(e);
    }

    @GetMapping
    public List<Equipement> findAll() {
        return equipementService.findAll();
    }

    @GetMapping("/{id}")
    public Equipement findById(@PathVariable Long id) {
        return equipementService.findById(id);
    }

    @PutMapping("/{id}")
    public Equipement update(@PathVariable Long id, @RequestBody Equipement e) {
        return equipementService.update(id, e);
    }

    @PutMapping("/{id}/etat")
    public Equipement updateEtat(@PathVariable Long id, @RequestParam Etat etat) {
        return equipementService.updateEtat(id, etat);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        equipementService.delete(id);
    }
}