package com.example.gestionlabo.Entites;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import com.example.gestionlabo.Enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nom;

    @NotBlank
    private String prenom;

    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String cin;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "is_chef_departement")
    private boolean chefDepartement = false;

    @ManyToOne
    @JoinColumn(name="departement_id")
    private Departement departement;

    @ManyToOne
    @JoinColumn(name="laboratoire_id")
    private Laboratoire laboratoire;

    @OneToMany(mappedBy = "auteur")
    @JsonIgnore
    private List<Reclamation> reclamations;

    @OneToMany(mappedBy = "etudiant")
    @JsonIgnore
    private List<Reservation> reservations;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
