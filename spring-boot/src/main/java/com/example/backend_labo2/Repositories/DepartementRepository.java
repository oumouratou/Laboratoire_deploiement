package com.example.backend_labo2.Repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend_labo2.Entites.*;
public interface DepartementRepository extends JpaRepository<Departement,Long> {
	
}

