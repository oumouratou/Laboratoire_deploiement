package com.example.backend_labo2.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend_labo2.Entites.Equipement;
import com.example.backend_labo2.Entites.Laboratoire;

@Repository
public interface EquipementRepository extends JpaRepository<Equipement, Long> {

    // Recherche par laboratoire (différentes méthodes)
    List<Equipement> findByLaboratoireId(Long laboratoireId);
    List<Equipement> findByLaboratoire(Laboratoire laboratoire);

    // Recherche par identifiant unique
    Optional<Equipement> findByIdentifiant(String identifiant);

    // Vérifier si un identifiant existe
    boolean existsByIdentifiant(String identifiant);

    // Vérifier doublon nom + laboratoire
    boolean existsByNomAndLaboratoireId(String nom, Long laboratoireId);

    // Recherche par nom ou identifiant
    @Query("SELECT e FROM Equipement e WHERE LOWER(e.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.identifiant) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Equipement> searchByNomOrIdentifiant(@Param("search") String search);

    // Suppression par laboratoire
    @Modifying
    @Query("DELETE FROM Equipement e WHERE e.laboratoire.id = :laboId")
    void deleteByLaboratoireId(@Param("laboId") Long laboId);

   
    
    @Modifying
    @Query("DELETE FROM Equipement e WHERE e.laboratoire.id = :laboId")
    void deleteAllByLaboratoireId(@Param("laboId") Long laboId);

    // Compter les réclamations en attente pour un équipement
    @Query("SELECT COUNT(r) FROM Reclamation r WHERE r.equipement.id = :equipementId AND r.etat = 'NON_TRAITEE'")
    long countPendingReclamationsByEquipementId(@Param("equipementId") Long equipementId);
}