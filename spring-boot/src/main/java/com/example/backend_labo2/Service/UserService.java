package com.example.backend_labo2.Service;

import com.example.backend_labo2.DTO.*;
import com.example.backend_labo2.Entites.Departement;
import com.example.backend_labo2.Entites.User;
import com.example.backend_labo2.Enums.Role;
import com.example.backend_labo2.Repositories.*;
import com.example.backend_labo2.jwt_util_java.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final DepartementRepository departementRepository;
    private final LaboratoireRepository laboratoireRepository;
    private final ReservationRepository reservationRepository;
    private final ReclamationRepository reclamationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private final AttestationVerificationService attestationVerificationService;
    private final AccountNotificationService accountNotificationService;

    private static final Pattern GMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@gmail\\.com$");

    public AuthReponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Identifiants invalides"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        if (!user.isActive()) {
            throw new RuntimeException("Compte désactivé");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return buildAuthResponse(user, token);
    }

    public AuthReponse register(RegisterRequest request) {
        return register(request, null);
    }

    public AuthReponse register(RegisterRequest request, MultipartFile attestation) {
        validateRegisterInput(request);

        User user = buildUserFromRegisterRequest(request, attestation);
        User saved = userRepository.save(user);

        accountNotificationService.sendAccountCreatedEmail(saved);

        String token = jwtUtil.generateToken(saved.getEmail(), saved.getRole().name());
        return buildAuthResponse(saved, token);
    }

    private void validateRegisterInput(RegisterRequest request) {
        if (request == null) throw new RuntimeException("Requête vide");
        if (isBlank(request.getNom()) || isBlank(request.getPrenom())
                || isBlank(request.getEmail()) || isBlank(request.getPassword())
                || isBlank(request.getCin())) {
            throw new RuntimeException("Nom, prénom, email, mot de passe et CIN sont obligatoires");
        }

        String email = normalizeEmail(request.getEmail());
        request.setEmail(email);

        if (!GMAIL_PATTERN.matcher(email).matches()) {
            throw new RuntimeException("Email invalide: utilisez une adresse Gmail valide");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email déjà utilisé");
        }

        String cin = request.getCin().trim().toUpperCase(Locale.ROOT);
        request.setCin(cin);

        if (userRepository.existsByCin(cin)) {
            throw new RuntimeException("Ce CIN existe déjà");
        }

        if (request.getRole() == null) {
            request.setRole(Role.ETUDIANT);
        }

        if (request.getRole() != Role.TECHNICIEN && request.getDepartementId() == null) {
            throw new RuntimeException("Département obligatoire");
        }

        if (request.getRole() == Role.ETUDIANT) {
            if (isBlank(request.getNiveau()) || isBlank(request.getClasse())) {
                throw new RuntimeException("Niveau et classe sont obligatoires pour un étudiant");
            }
        }
    }

    private User buildUserFromRegisterRequest(RegisterRequest request, MultipartFile attestation) {
        User user = new User();
        user.setNom(request.getNom().trim());
        user.setPrenom(request.getPrenom().trim());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCin(request.getCin());
        user.setRole(request.getRole());
        user.setActive(true);
        user.setChefDepartement(request.getRole() == Role.ENSEIGNANT && Boolean.TRUE.equals(request.getIsChefDepartement()));
        user.setCreatedAt(LocalDateTime.now());

        if (request.getDepartementId() != null) {
            Departement dept = departementRepository.findById(request.getDepartementId())
                    .orElseThrow(() -> new RuntimeException("Département introuvable"));
            user.setDepartement(dept);

            if (request.getRole() == Role.ETUDIANT) {
                user.setNiveau(request.getNiveau().trim());
                user.setClasse(request.getClasse().trim().toUpperCase(Locale.ROOT));

                // Attestation est maintenant optionnelle
                if (attestation != null && !attestation.isEmpty()) {
                    AttestationVerificationService.StoredAttestation stored =
                            attestationVerificationService.verifyAndStore(
                                    attestation,
                                    request.getCin(),
                                    request.getNom(),
                                    request.getPrenom(),
                                    dept.getNom(),
                                    request.getNiveau(),
                                    request.getClasse()
                            );

                    user.setAttestationUrl(stored.url());
                    user.setAttestationOriginalName(stored.originalName());
                    user.setAttestationMimeType(stored.mimeType());
                    user.setAttestationSize(stored.size());
                    user.setAttestationVerifiee(stored.verified());
                }
            }
        }

        return user;
    }

    private AuthReponse buildAuthResponse(User user, String token) {
        AuthReponse.AuthReponseBuilder builder = AuthReponse.builder()
                .token(token)
                .id(user.getId())
                .email(user.getEmail())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .role(user.getRole())
                .cin(user.getCin())
                .dateCreation(user.getCreatedAt())
                .chefDepartement(user.isChefDepartement())
                .niveau(user.getNiveau())
                .classe(user.getClasse())
                .attestationUrl(user.getAttestationUrl())
                .attestationVerifiee(user.getAttestationVerifiee());

        if (user.getDepartement() != null) {
            builder.departementId(user.getDepartement().getId());
            builder.departementNom(user.getDepartement().getNom());
        }

        return builder.build();
    }

    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        userRepository.findAll().forEach(list::add);
        return list;
    }

    public List<User> findEtudiants() {
        return userRepository.findByRole(Role.ETUDIANT);
    }

    public List<User> findEnseignants() {
        return userRepository.findByRole(Role.ENSEIGNANT);
    }

    public User createEtudiant(EtudiantCreateRequest dto) {
        User u = createUser(dto.getNom(), dto.getPrenom(), dto.getEmail(),
                dto.getPassword(), dto.getCin(), Role.ETUDIANT, dto.getDepartementId());

        return userRepository.save(u);
    }

    public User updateEtudiant(Long id, EtudiantCreateRequest dto) {
        User user = updateUser(id, dto.getNom(), dto.getPrenom(), dto.getEmail(),
                dto.getPassword(), dto.getCin(), dto.getDepartementId());
        
        return userRepository.save(user);
    }

    public User createEnseignant(EnseignantCreateRequest dto) {
        User user = createUser(dto.getNom(), dto.getPrenom(), dto.getEmail(),
                dto.getPassword(), dto.getCin(), Role.ENSEIGNANT, dto.getDepartementId());
        user.setChefDepartement(Boolean.TRUE.equals(dto.getIsChefDepartement()));
        return userRepository.save(user);
    }

    public User updateEnseignant(Long id, EnseignantUpdateRequest dto) {
        User user = updateUser(id, dto.getNom(), dto.getPrenom(), dto.getEmail(),
                dto.getPassword(), dto.getCin(), dto.getDepartementId());
        if (dto.getIsChefDepartement() != null) {
            user.setChefDepartement(dto.getIsChefDepartement());
        }
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        reclamationRepository.deleteAllByAuteur(user);
        reservationRepository.deleteAllByEtudiant(user);
        userRepository.delete(user);
    }

    private User createUser(String nom, String prenom, String email, String password,
                            String cin, Role role, Long departementId) {
        String emailNormalized = normalizeEmail(email);
        String cinNormalized = cin == null ? null : cin.trim().toUpperCase(Locale.ROOT);

        if (!GMAIL_PATTERN.matcher(emailNormalized).matches()) {
            throw new RuntimeException("Email invalide: utilisez une adresse Gmail valide");
        }
        if (userRepository.existsByEmail(emailNormalized)) {
            throw new RuntimeException("Email déjà utilisé");
        }
        if (cinNormalized != null && userRepository.existsByCin(cinNormalized)) {
            throw new RuntimeException("Ce CIN existe déjà");
        }

        User user = new User();
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setEmail(emailNormalized);
        user.setPassword(passwordEncoder.encode(password));
        user.setCin(cinNormalized);
        user.setRole(role);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        if (departementId != null) {
            Departement dept = departementRepository.findById(departementId)
                    .orElseThrow(() -> new RuntimeException("Département introuvable"));
            user.setDepartement(dept);
        }

        return userRepository.save(user);
    }

    private User updateUser(Long id, String nom, String prenom, String email, String password,
                            String cin, Long departementId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        String emailNormalized = normalizeEmail(email);
        String cinNormalized = cin == null ? null : cin.trim().toUpperCase(Locale.ROOT);

        if (!user.getEmail().equalsIgnoreCase(emailNormalized) && userRepository.existsByEmail(emailNormalized)) {
            throw new RuntimeException("Email déjà utilisé");
        }

        if (cinNormalized != null && !cinNormalized.equalsIgnoreCase(user.getCin()) && userRepository.existsByCin(cinNormalized)) {
            throw new RuntimeException("Ce CIN existe déjà");
        }

        user.setNom(nom);
        user.setPrenom(prenom);
        user.setEmail(emailNormalized);
        user.setCin(cinNormalized);

        if (password != null && !password.isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        if (departementId != null) {
            Departement dep = departementRepository.findById(departementId)
                    .orElseThrow(() -> new RuntimeException("Département introuvable"));
            user.setDepartement(dep);
        }

        return userRepository.save(user);
    }

    @Transactional
    public User blockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        user.setActive(false);
        return userRepository.save(user);
    }

    @Transactional
    public User unblockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        user.setActive(true);
        return userRepository.save(user);
    }

    private String normalizeEmail(String email) {
        if (email == null) return "";
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isBlank(String v) {
        return v == null || v.trim().isEmpty();
    }
}