package com.example.gestionlabo.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gestionlabo.DTO.ReservationRequest;
import com.example.gestionlabo.Entites.Laboratoire;
import com.example.gestionlabo.Entites.Reservation;
import com.example.gestionlabo.Entites.User;
import com.example.gestionlabo.Enums.StatutReservation;
import com.example.gestionlabo.Repositories.LaboratoireRepository;
import com.example.gestionlabo.Repositories.ReservationRepository;
import com.example.gestionlabo.Repositories.UserRepository;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LaboratoireRepository laboratoireRepository;

    // ✅ CREATE
    @Transactional
    public Reservation create(ReservationRequest request) {

        User etudiant = userRepository.findById(request.getEtudiantId())
                .orElseThrow(() -> new RuntimeException("Etudiant introuvable"));

        Laboratoire laboratoire = laboratoireRepository.findById(request.getLaboratoireId())
                .orElseThrow(() -> new RuntimeException("Laboratoire introuvable"));

        Reservation reservation = new Reservation();
        reservation.setEtudiant(etudiant);
        reservation.setLaboratoire(laboratoire);
        reservation.setDateReservation(request.getDateReservation());
        reservation.setHeureDebut(request.getHeureDebut());
        reservation.setHeureFin(request.getHeureFin());
        reservation.setMotif(request.getMotif());
        reservation.setStatut(StatutReservation.EN_ATTENTE);

        return reservationRepository.save(reservation);
    }

    // ✅ READ ALL
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    // ✅ READ BY ID
    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation introuvable"));
    }

    // ✅ READ BY ETUDIANT
    public List<Reservation> findByEtudiant(Long etudiantId) {
        return reservationRepository.findByEtudiantId(etudiantId);
    }

    // ✅ ANNULER
    @Transactional
    public Reservation annuler(Long id) {
        Reservation r = findById(id);
        r.setStatut(StatutReservation.ANNULEE);
        return reservationRepository.save(r);
    }

    // ✅ DELETE
    @Transactional
    public void delete(Long id) {
        reservationRepository.deleteById(id);
    }
}
