package com.example.gestionlabo.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.gestionlabo.DTO.ReservationRequest;
import com.example.gestionlabo.Entites.Reservation;
import com.example.gestionlabo.Service.ReservationService;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin("*")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    // ✅ CREATE
    @PostMapping
    public Reservation create(@RequestBody ReservationRequest request) {
        return reservationService.create(request);
    }

    // ✅ READ ALL
    @GetMapping
    public List<Reservation> findAll() {
        return reservationService.findAll();
    }

    // ✅ READ BY ID
    @GetMapping("/{id}")
    public Reservation findById(@PathVariable Long id) {
        return reservationService.findById(id);
    }

    // ✅ READ BY ETUDIANT
    @GetMapping("/etudiant/{etudiantId}")
    public List<Reservation> findByEtudiant(@PathVariable Long etudiantId) {
        return reservationService.findByEtudiant(etudiantId);
    }

    // ✅ ANNULER
    @PutMapping("/{id}/annuler")
    public Reservation annuler(@PathVariable Long id) {
        return reservationService.annuler(id);
    }

    // ✅ DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        reservationService.delete(id);
    }
}
