package com.example.gestionlabo.Repositories;

import com.example.gestionlabo.Entites.Equipement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EquipementRepository extends JpaRepository<Equipement, Long> {
    List<Equipement> findByLaboratoireId(Long laboratoireId);
}
