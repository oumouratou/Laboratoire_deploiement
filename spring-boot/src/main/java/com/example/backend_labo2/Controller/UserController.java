package com.example.backend_labo2.Controller;
import com.example.backend_labo2.DTO.*;
import com.example.backend_labo2.Entites.User;
import com.example.backend_labo2.Repositories.UserRepository;
import com.example.backend_labo2.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.nio.file.*;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@GetMapping
	public List<User> findAll() {
	    return userService.findAll();
	}

	@GetMapping("/enseignants")
	public List<User> getEnseignants() {
	    return userService.findEnseignants();
	}

	@PostMapping("/enseignants")
	public User createEnseignant(@RequestBody EnseignantCreateRequest dto) {
	    return userService.createEnseignant(dto);
	}

	@PutMapping("/enseignants/{id}")
	public User updateEnseignant(@PathVariable Long id, @RequestBody EnseignantUpdateRequest dto) {
	    return userService.updateEnseignant(id, dto);
	}

	@GetMapping("/etudiants")
	public List<User> getEtudiants() {
	    return userService.findEtudiants();
	}

	@PostMapping("/etudiants")
	public User createEtudiant(@RequestBody EtudiantCreateRequest dto) {
	    return userService.createEtudiant(dto);
	}

	@PutMapping("/etudiants/{id}")
	public User updateEtudiant(@PathVariable Long id, @RequestBody EtudiantCreateRequest dto) {
	    return userService.updateEtudiant(id, dto);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteUser(@PathVariable Long id) {
	    userService.deleteUser(id);
	    return ResponseEntity.ok("Utilisateur supprimé");
	}

	@GetMapping("/profil")
	public ResponseEntity<?> getProfil(Principal principal) {
	    try {
	        User user = userRepository.findByEmail(principal.getName())
	                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

	        Map<String, Object> response = new HashMap<>();
	        response.put("id", user.getId());
	        response.put("nom", user.getNom());
	        response.put("prenom", user.getPrenom());
	        response.put("email", user.getEmail());
	        response.put("role", user.getRole());
	        response.put("isChefDepartement", user.isChefDepartement());
	        response.put("niveau", user.getNiveau());
	        response.put("classe", user.getClasse());
	        response.put("attestationUrl", user.getAttestationUrl());
	        response.put("attestationVerifiee", user.getAttestationVerifiee());
	        return ResponseEntity.ok(response);
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("error", e.getMessage()));
	    }
	}

	@PutMapping("/profil")
	public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> data, Principal principal) {
	    try {
	        User user = userRepository.findByEmail(principal.getName())
	                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

	        if (data.containsKey("nom")) user.setNom(data.get("nom"));
	        if (data.containsKey("prenom")) user.setPrenom(data.get("prenom"));
	        if (data.containsKey("email")) user.setEmail(data.get("email"));
	        if (data.containsKey("cin")) user.setCin(data.get("cin"));

	        userRepository.save(user);
	        return ResponseEntity.ok(Map.of("message", "Profil mis à jour"));
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("error", e.getMessage()));
	    }
	}

	@PostMapping("/photo")
	public ResponseEntity<?> uploadPhoto(@RequestParam("photo") MultipartFile file, Principal principal) {
	    try {
	        User user = userRepository.findByEmail(principal.getName())
	                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

	        String contentType = file.getContentType();
	        if (contentType == null || !contentType.startsWith("image/")) {
	            return ResponseEntity.badRequest().body(Map.of("error", "Le fichier doit être une image"));
	        }

	        String filename = "user_" + user.getId() + "_" + System.currentTimeMillis() + ".jpg";
	        Path uploadPath = Paths.get("uploads/photos/");

	        if (!Files.exists(uploadPath)) {
	            Files.createDirectories(uploadPath);
	        }

	        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
	            try {
	                Path oldPhoto = Paths.get(user.getPhotoUrl().startsWith("/")
	                        ? user.getPhotoUrl().substring(1)
	                        : user.getPhotoUrl());
	                Files.deleteIfExists(oldPhoto);
	            } catch (Exception ignored) {
	            }
	        }

	        Path filePath = uploadPath.resolve(filename);
	        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

	        user.setPhotoUrl("/uploads/photos/" + filename);
	        userRepository.save(user);

	        return ResponseEntity.ok(Map.of(
	                "message", "Photo uploadée avec succès",
	                "url", user.getPhotoUrl()
	        ));
	    } catch (IOException e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("error", "Erreur lors de l'upload: " + e.getMessage()));
	    }
	}

	@GetMapping("/photo")
	public ResponseEntity<?> getPhoto(Principal principal) {
	    try {
	        User user = userRepository.findByEmail(principal.getName())
	                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

	        if (user.getPhotoUrl() == null || user.getPhotoUrl().isEmpty()) {
	            return ResponseEntity.noContent().build();
	        }

	        String photoPath = user.getPhotoUrl();
	        if (photoPath.startsWith("/")) {
	            photoPath = photoPath.substring(1);
	        }

	        Path path = Paths.get(photoPath);
	        if (!Files.exists(path)) {
	            return ResponseEntity.noContent().build();
	        }

	        byte[] data = Files.readAllBytes(path);
	        String contentType = Files.probeContentType(path);
	        if (contentType == null) contentType = "image/jpeg";

	        return ResponseEntity.ok()
	                .contentType(MediaType.parseMediaType(contentType))
	                .body(data);
	    } catch (IOException e) {
	        return ResponseEntity.noContent().build();
	    }
	}

	@PutMapping("/password")
	public ResponseEntity<?> changePassword(@RequestBody Map<String, String> data, Principal principal) {
	    try {
	        User user = userRepository.findByEmail(principal.getName())
	                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

	        String ancien = data.get("ancienPassword");
	        String nouveau = data.get("nouveauPassword");

	        if (ancien == null || nouveau == null) {
	            return ResponseEntity.badRequest().body(Map.of("message", "Ancien et nouveau mot de passe requis"));
	        }

	        if (!passwordEncoder.matches(ancien, user.getPassword())) {
	            return ResponseEntity.badRequest().body(Map.of("message", "Ancien mot de passe incorrect"));
	        }

	        user.setPassword(passwordEncoder.encode(nouveau));
	        userRepository.save(user);

	        return ResponseEntity.ok(Map.of("message", "Mot de passe modifié avec succès"));
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("error", e.getMessage()));
	    }
	}

	@PutMapping("/{id}/block")
	public ResponseEntity<?> blockUser(@PathVariable Long id) {
	    try {
	        User user = userService.blockUser(id);
	        return ResponseEntity.ok(Map.of(
	                "message", "Utilisateur bloqué",
	                "id", user.getId(),
	                "active", user.isActive()
	        ));
	    } catch (RuntimeException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(Map.of("error", e.getMessage()));
	    }
	}

	@PutMapping("/{id}/unblock")
	public ResponseEntity<?> unblockUser(@PathVariable Long id) {
	    try {
	        User user = userService.unblockUser(id);
	        return ResponseEntity.ok(Map.of(
	                "message", "Utilisateur débloqué",
	                "id", user.getId(),
	                "active", user.isActive()
	        ));
	    } catch (RuntimeException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(Map.of("error", e.getMessage()));
	    }
	}

	@PutMapping("/etudiants/{id}/block")
	public ResponseEntity<?> blockEtudiant(@PathVariable Long id) {
	    return blockUser(id);
	}

	@PutMapping("/etudiants/{id}/unblock")
	public ResponseEntity<?> unblockEtudiant(@PathVariable Long id) {
	    return unblockUser(id);
	}

}
