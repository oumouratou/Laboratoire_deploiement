package com.example.gestionlabo.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.gestionlabo.Entites.Laboratoire;
import java.util.List;

public interface LaboratoireRepository extends JpaRepository<Laboratoire, Long> {
    List<Laboratoire> findByDepartementId(Long departementId);
}
