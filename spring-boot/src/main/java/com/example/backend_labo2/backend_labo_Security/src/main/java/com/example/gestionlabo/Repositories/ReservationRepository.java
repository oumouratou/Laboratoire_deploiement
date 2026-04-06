package com.example.gestionlabo.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.gestionlabo.Entites.Reservation;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // Réservations par étudiant
    List<Reservation> findByEtudiantId(Long etudiantId);
}
