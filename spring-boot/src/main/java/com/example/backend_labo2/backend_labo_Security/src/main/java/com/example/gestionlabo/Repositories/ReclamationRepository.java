package com.example.gestionlabo.Repositories;

import com.example.gestionlabo.Entites.Reclamation;
import com.example.gestionlabo.Entites.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReclamationRepository extends JpaRepository<Reclamation, Long> {
    List<Reclamation> findByAuteur(User auteur);
    List<Reclamation> findByAuteurId(Long auteurId);
}
