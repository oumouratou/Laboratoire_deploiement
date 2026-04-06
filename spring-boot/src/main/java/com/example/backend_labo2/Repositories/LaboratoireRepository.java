package com.example.backend_labo2.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository; // <-- changer ici
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.backend_labo2.Entites.Laboratoire;

@Repository
public interface LaboratoireRepository extends JpaRepository<Laboratoire, Long> { // <-- JpaRepository

    List<Laboratoire> findByDepartementId(Long departementId);

    @Modifying
    @Query("DELETE FROM Laboratoire l WHERE l.departement.id = :departementId")
    void deleteByDepartementId(Long departementId);
    
    Laboratoire findByNomLabo(String nomLabo);
    
    
}
