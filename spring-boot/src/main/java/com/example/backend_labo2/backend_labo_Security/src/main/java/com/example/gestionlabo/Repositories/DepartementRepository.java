package com.example.gestionlabo.Repositories;
import com.example.gestionlabo.Entites.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
public interface DepartementRepository extends JpaRepository<Departement,Long> {
	
}

