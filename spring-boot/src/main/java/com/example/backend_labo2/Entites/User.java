package com.example.backend_labo2.Entites;

import com.example.backend_labo2.Enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "user")
public class User {
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     private String nom;
     private String prenom;

     @Column(unique = true, nullable = false)
     private String email;

     @Column(nullable = false)
     private String password;

     @Column(unique = true)
     private String cin;

     @Enumerated(EnumType.STRING)
     @Column(nullable = false)
     private Role role;

     @Column(name = "is_active", nullable = false)
     private Boolean active = true;

     @ManyToOne
     @JoinColumn(name = "departement_id")
     private Departement departement;

     @ManyToOne
     @JoinColumn(name = "laboratoire_id")
     private Laboratoire laboratoire;

     @OneToMany(mappedBy = "auteur")
     @JsonIgnore
     private List<Reclamation> reclamations;

     @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
     private LocalDateTime createdAt;

     @Column(name = "is_chef_departement", nullable = false)
     private boolean chefDepartement = false;

     private String photoUrl;

     @Column(length = 30)
     private String niveau;

     @Column(length = 30)
     private String classe;

     @Column(length = 255)
     private String attestationUrl;

     @Column(length = 255)
     private String attestationOriginalName;

     @Column(length = 100)
     private String attestationMimeType;

     private Long attestationSize;

     private Boolean attestationVerifiee = false;
     
     public boolean isActive() {
         return Boolean.TRUE.equals(active);
     }

     public void setActive(Boolean active) {
         this.active = active;
     }
         
     @PrePersist
     protected void onCreate() {
         this.createdAt = LocalDateTime.now();
         if (this.active == null) this.active = true;
         if (this.attestationVerifiee == null) this.attestationVerifiee = false;
     }
}