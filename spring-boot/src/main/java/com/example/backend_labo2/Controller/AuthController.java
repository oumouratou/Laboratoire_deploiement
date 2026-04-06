package com.example.backend_labo2.Controller;

import com.example.backend_labo2.DTO.*;
import com.example.backend_labo2.Enums.Role;
import com.example.backend_labo2.Service.PasswordResetService;
import com.example.backend_labo2.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor   // ← C'était manquant : génère le constructeur pour les champs final
public class AuthController {

    private final UserService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerJson(@RequestBody RegisterRequest request) {
        try {
            AuthReponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerMultipart(
            @RequestParam("nom") String nom,
            @RequestParam("prenom") String prenom,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("cin") String cin,
            @RequestParam("role") String role,
            @RequestParam(value = "departementId", required = false) Long departementId,
            @RequestParam(value = "isChefDepartement", required = false) Boolean isChefDepartement,
            @RequestParam(value = "niveau", required = false) String niveau,
            @RequestParam(value = "classe", required = false) String classe,
            @RequestPart(value = "attestation", required = false) MultipartFile attestation
    ) {
        try {
            RegisterRequest req = RegisterRequest.builder()
                    .nom(nom)
                    .prenom(prenom)
                    .email(email)
                    .password(password)
                    .cin(cin)
                    .role(Role.valueOf(role.toUpperCase()))
                    .departementId(departementId)
                    .isChefDepartement(isChefDepartement)
                    .niveau(niveau)
                    .classe(classe)
                    .build();

            AuthReponse response = authService.register(req, attestation);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthReponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email requis"));
            }
            passwordResetService.createPasswordResetToken(email);
            return ResponseEntity.ok(Map.of(
                    "message", "Si un compte existe avec cet email, un lien de réinitialisation a été envoyé."
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "message", "Si un compte existe avec cet email, un lien de réinitialisation a été envoyé."
            ));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword");

            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Token requis"));
            }
            if (newPassword == null || newPassword.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("message", "Le mot de passe doit contenir au moins 6 caractères"));
            }

            passwordResetService.resetPassword(token, newPassword);
            return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/verify-reset-token")
    public ResponseEntity<?> verifyResetToken(@RequestParam String token) {
        try {
            boolean valid = passwordResetService.validatePasswordResetToken(token);
            return ResponseEntity.ok(Map.of("valid", valid));
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("valid", false);
            result.put("message", e.getMessage());
            return ResponseEntity.ok(result);
        }
    }
    
    
}