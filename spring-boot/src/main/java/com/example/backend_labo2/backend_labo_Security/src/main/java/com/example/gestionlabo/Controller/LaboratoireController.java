package com.example.gestionlabo.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.gestionlabo.Config.JwtUtil;
import com.example.gestionlabo.Entites.Laboratoire;
import com.example.gestionlabo.Entites.User;
import com.example.gestionlabo.Repositories.LaboratoireRepository;
import com.example.gestionlabo.Repositories.UserRepository;
import com.example.gestionlabo.Service.LaboratoireService;
import com.example.gestionlabo.Enums.Etat;

@RestController
@RequestMapping("/api/laboratoires")
@CrossOrigin("*")
public class LaboratoireController {

    @Autowired private LaboratoireService laboratoireService;
    @Autowired private LaboratoireRepository laboratoireRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserRepository userRepository;

    private User extractUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) return null;
        String email = jwtUtil.extractEmail(token);
        return userRepository.findByEmail(email).orElse(null);
    }

    @PostMapping
    public Laboratoire create(@RequestBody Laboratoire labo) {
        return laboratoireService.create(labo);
    }

    @GetMapping
    public List<Laboratoire> findAll() {
        return laboratoireService.findAll();
    }

    @GetMapping("/{id}")
    public Laboratoire findById(@PathVariable Long id) {
        return laboratoireService.findById(id);
    }

    @PutMapping("/{id}")
    public Laboratoire update(@PathVariable Long id, @RequestBody Laboratoire labo) {
        return laboratoireService.update(id, labo);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        laboratoireService.delete(id);
    }

    @GetMapping("/etats")
    public Etat[] getEtats() {
        return Etat.values();
    }

    // Mes labos - filter by user's department
    @GetMapping("/mes-labos")
    public ResponseEntity<List<Laboratoire>> getMesLabos(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = extractUser(authHeader);
        if (user != null && user.getDepartement() != null) {
            return ResponseEntity.ok(
                laboratoireRepository.findByDepartementId(user.getDepartement().getId())
            );
        }
        // Fallback: return all
        return ResponseEntity.ok(laboratoireService.findAll());
    }
}
