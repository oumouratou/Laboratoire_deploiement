

package com.example.backend_labo2.Repositories;

import com.example.backend_labo2.Entites.User;
import com.example.backend_labo2.Enums.Role;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
	 @Modifying
	 @Query("UPDATE User u SET u.departement = null WHERE u.departement.id = :departementId")
	 void detachByDepartementId(Long departementId);

	 @Modifying
	 @Query("DELETE FROM User u WHERE u.laboratoire.id = :laboId")
	 void deleteAllByLaboratoireId(Long laboId);

	 boolean existsByEmail(String email);

	 Optional<User> findByEmail(String email);

	 boolean existsByCin(String cin);

	 List<User> findByRole(Role role);

	 List<User> findByDepartementIdAndRole(Long departementId, Role role);

	 List<User> findByDepartementId(Long departementId);

}