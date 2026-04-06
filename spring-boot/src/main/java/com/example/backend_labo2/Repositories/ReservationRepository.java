package com.example.backend_labo2.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend_labo2.Entites.Reservation;
import com.example.backend_labo2.Entites.User;
import com.example.backend_labo2.Enums.StatutReservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r LEFT JOIN FETCH r.etudiant LEFT JOIN FETCH r.laboratoire")
    List<Reservation> findAllWithEtudiantAndLaboratoire();

    @Query("SELECT r FROM Reservation r LEFT JOIN FETCH r.laboratoire WHERE r.etudiant.id = :etudiantId")
    List<Reservation> findByEtudiantWithLaboratoire(@Param("etudiantId") Long etudiantId);

    List<Reservation> findByEtudiantId(Long etudiantId);

    // Pour trouver les réservations par labo
    List<Reservation> findByLaboratoireId(Long laboratoireId);

    // Pour trouver les réservations approuvées d'un labo
    List<Reservation> findByLaboratoireIdAndStatut(Long laboratoireId, StatutReservation statut);

    @Query("SELECT r FROM Reservation r WHERE r.laboratoire.id = :laboId AND r.statut = 'APPROUVEE'")
    List<Reservation> findApprovedByLaboratoireId(@Param("laboId") Long laboId);

    // Supprimer toutes les réservations d'un labo
    @Modifying
    @Query("DELETE FROM Reservation r WHERE r.laboratoire.id = :laboId")
    void deleteAllByLaboratoireId(@Param("laboId") Long laboId);
    void deleteAllByEtudiant(User etudiant);
    
}