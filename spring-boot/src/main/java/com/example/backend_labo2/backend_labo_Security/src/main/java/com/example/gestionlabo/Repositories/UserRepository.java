package com.example.gestionlabo.Repositories;

import com.example.gestionlabo.Entites.User;
import com.example.gestionlabo.Enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByRole(Role role);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);
}
