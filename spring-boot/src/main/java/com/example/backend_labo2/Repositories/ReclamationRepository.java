package com.example.backend_labo2.Repositories;

import com.example.backend_labo2.Entites.Laboratoire;
import com.example.backend_labo2.Entites.Reclamation;
import com.example.backend_labo2.Entites.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReclamationRepository extends JpaRepository<Reclamation, Long> {
    
    List<Reclamation> findByAuteurEmail(String email);
    
    List<Reclamation> findByAuteurEmailOrderByDateReclamationDesc(String email);
    
    List<Reclamation> findByAuteur(User auteur);
    
    List<Reclamation> findByAuteurOrderByDateReclamationDesc(User auteur);
    
    List<Reclamation> findAllByOrderByDateReclamationDesc();
    
    @Query("SELECT r FROM Reclamation r LEFT JOIN FETCH r.equipement LEFT JOIN FETCH r.laboratoire WHERE r.laboratoire = :labo ORDER BY r.dateReclamation DESC")
    List<Reclamation> findByLaboratoireWithDetails(@Param("labo") Laboratoire labo);
    
    @Modifying
    @Query("DELETE FROM Reclamation r WHERE r.equipement.id = :equipementId")
    void deleteByEquipementId(@Param("equipementId") Long equipementId);
    
    // Trouver les réclamations par équipement
    List<Reclamation> findByEquipementId(Long equipementId);
        void deleteAllByAuteur(User auteur);
}