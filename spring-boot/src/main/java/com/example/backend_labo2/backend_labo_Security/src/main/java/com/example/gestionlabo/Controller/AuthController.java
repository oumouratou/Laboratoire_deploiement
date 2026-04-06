package com.example.gestionlabo.Controller;

import com.example.gestionlabo.Config.JwtUtil;
import com.example.gestionlabo.Entites.User;
import com.example.gestionlabo.Enums.Role;
import com.example.gestionlabo.Repositories.DepartementRepository;
import com.example.gestionlabo.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DepartementRepository departementRepository;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email") == null ? null : credentials.get("email").trim().toLowerCase();
        String password = credentials.get("password");

        if (email == null || email.isEmpty() || password == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Email ou mot de passe incorrect"));
        }

        Optional<User> optUser = userRepository.findByEmailIgnoreCase(email);
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Email ou mot de passe incorrect"));
        }

        User user = optUser.get();
        String storedPassword = user.getPassword();
        boolean isPasswordValid = storedPassword != null && storedPassword.equals(password);

        // Backward compatibility: some existing accounts may already be stored with
        // BCrypt hashes.
        if (!isPasswordValid && storedPassword != null && storedPassword.startsWith("$2")) {
            try {
                isPasswordValid = BCrypt.checkpw(password, storedPassword);
            } catch (IllegalArgumentException ignored) {
                isPasswordValid = false;
            }
        }

        if (!isPasswordValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Email ou mot de passe incorrect"));
        }

        if (!user.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Votre compte a ete bloque. Veuillez contacter l'administration."));
        }

        String token = jwtUtil.generateToken(user.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("id", user.getId());
        response.put("nom", user.getNom());
        response.put("prenom", user.getPrenom());
        response.put("email", user.getEmail());
        response.put("cin", user.getCin());
        response.put("role", user.getRole().name());
        response.put("active", user.isActive());
        response.put("chefDepartement", user.isChefDepartement());
        if (user.getDepartement() != null) {
            Map<String, Object> dept = new HashMap<>();
            dept.put("id", user.getDepartement().getId());
            dept.put("nom", user.getDepartement().getNom());
            response.put("departement", dept);
            response.put("departementId", user.getDepartement().getId());
            response.put("departementNom", user.getDepartement().getNom());
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {
        String email = ((String) body.get("email")).trim().toLowerCase();

        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email deja utilise"));
        }

        User user = new User();
        user.setNom((String) body.get("nom"));
        user.setPrenom((String) body.get("prenom"));
        user.setEmail(email);
        user.setPassword((String) body.get("password"));
        user.setCin((String) body.get("cin"));
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        String roleStr = (String) body.get("role");
        user.setRole(Role.valueOf(roleStr.toUpperCase()));

        Object deptIdObj = body.get("departementId");
        if (deptIdObj != null) {
            Long departementId = Long.valueOf(deptIdObj.toString());
            user.setDepartement(
                    departementRepository.findById(departementId)
                            .orElseThrow(() -> new RuntimeException("Departement introuvable")));
        }

        Object chefObj = body.get("isChefDepartement");
        if (chefObj != null) {
            user.setChefDepartement(Boolean.parseBoolean(chefObj.toString()));
        }

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("id", saved.getId());
        response.put("nom", saved.getNom());
        response.put("prenom", saved.getPrenom());
        response.put("email", saved.getEmail());
        response.put("role", saved.getRole().name());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Non authentifie"));
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Token invalide"));
        }
        String email = jwtUtil.extractEmail(token);
        Optional<User> optUser = userRepository.findByEmail(email);
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Utilisateur introuvable"));
        }
        User user = optUser.get();
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("nom", user.getNom());
        response.put("prenom", user.getPrenom());
        response.put("email", user.getEmail());
        response.put("cin", user.getCin());
        response.put("role", user.getRole().name());
        response.put("active", user.isActive());
        response.put("chefDepartement", user.isChefDepartement());
        if (user.getDepartement() != null) {
            Map<String, Object> dept = new HashMap<>();
            dept.put("id", user.getDepartement().getId());
            dept.put("nom", user.getDepartement().getNom());
            response.put("departement", dept);
        }
        return ResponseEntity.ok(response);
    }
}
