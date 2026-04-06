package com.example.gestionlabo.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.gestionlabo.Config.JwtUtil;
import com.example.gestionlabo.Entites.Reclamation;
import com.example.gestionlabo.Entites.User;
import com.example.gestionlabo.Entites.Laboratoire;
import com.example.gestionlabo.Entites.Equipement;
import com.example.gestionlabo.Enums.Role;
import com.example.gestionlabo.Repositories.UserRepository;
import com.example.gestionlabo.Repositories.LaboratoireRepository;
import com.example.gestionlabo.Repositories.EquipementRepository;
import com.example.gestionlabo.Service.ReclamationService;

@RestController
@RequestMapping("/api/reclamations")
@CrossOrigin("*")
public class ReclamationController {

    @Autowired private ReclamationService reclamationService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserRepository userRepository;
    @Autowired private LaboratoireRepository laboratoireRepository;
    @Autowired private EquipementRepository equipementRepository;

    private User extractUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) return null;
        String email = jwtUtil.extractEmail(token);
        return userRepository.findByEmail(email).orElse(null);
    }

    // CREATE
    @PostMapping("/create")
    public Reclamation create(@RequestBody Reclamation r) {
        return reclamationService.create(r);
    }

    // READ ALL
    @GetMapping
    public List<Reclamation> findAll() {
        return reclamationService.findAll();
    }

    // GET /all - alias for findAll (technicien)
    @GetMapping("/all")
    public List<Reclamation> findAllAlias() {
        return reclamationService.findAll();
    }

    // GET /mes - my reclamations
    @GetMapping("/mes")
    public ResponseEntity<List<Reclamation>> getMesReclamations(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = extractUser(authHeader);
        if (user == null) return ResponseEntity.ok(reclamationService.findAll());
        return ResponseEntity.ok(reclamationService.findByAuteurId(user.getId()));
    }

    // GET /etudiant - reclamations for current student
    @GetMapping("/etudiant")
    public ResponseEntity<List<Reclamation>> getReclamationsEtudiant(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = extractUser(authHeader);
        if (user == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(reclamationService.findByAuteurId(user.getId()));
    }

    // GET /pour-technicien - all reclamations for technicien
    @GetMapping("/pour-technicien")
    public List<Reclamation> getReclamationsPourTechnicien() {
        return reclamationService.findAll();
    }

    // GET /technicien - alias
    @GetMapping("/technicien")
    public List<Reclamation> getReclamationsTechnicien() {
        return reclamationService.findAll();
    }

    // GET /pour-enseignant - reclamations for current enseignant
    @GetMapping("/pour-enseignant")
    public ResponseEntity<List<Reclamation>> getReclamationsEnseignant(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = extractUser(authHeader);
        if (user == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(reclamationService.findByAuteurId(user.getId()));
    }

    // READ BY ID
    @GetMapping("/{id}")
    public Reclamation findById(@PathVariable Long id) {
        return reclamationService.findById(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public Reclamation update(@PathVariable Long id, @RequestBody Reclamation r) {
        return reclamationService.update(id, r);
    }

    // TRAITER (simple - set TRAITEE)
    @PutMapping("/{id}/traiter")
    public Reclamation traiter(@PathVariable Long id) {
        return reclamationService.traiter(id);
    }

    // TRAITER avec action JSON body {action, motifRefus}
    @PutMapping("/{id}/traiter-json")
    public ResponseEntity<Reclamation> traiterJson(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        String action = body.getOrDefault("action", "TRAITEE");
        String motifRefus = body.getOrDefault("motifRefus", "");
        Reclamation r = reclamationService.traiterAvecAction(id, action, motifRefus);
        return ResponseEntity.ok(r);
    }

    // AUTO-ANNULER (par l'auteur)
    @PutMapping("/{id}/auto-annuler")
    public ResponseEntity<Reclamation> autoAnnuler(@PathVariable Long id) {
        return ResponseEntity.ok(reclamationService.autoAnnuler(id));
    }

    // GET laboratories by connected user department
    @GetMapping("/laboratoires/by-departement")
    public ResponseEntity<List<Laboratoire>> getLaboratoiresByDepartement(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = extractUser(authHeader);
        if (user != null && user.getDepartement() != null) {
            List<Laboratoire> labs = laboratoireRepository.findByDepartementId(user.getDepartement().getId());
            return ResponseEntity.ok(labs);
        }
        return ResponseEntity.ok(laboratoireRepository.findAll());
    }

    // GET equipements by labo
    @GetMapping("/by-labo/{laboId}")
    public ResponseEntity<List<Equipement>> getEquipementsByLabo(@PathVariable Long laboId) {
        List<Equipement> equips = equipementRepository.findByLaboratoireId(laboId);
        return ResponseEntity.ok(equips);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        reclamationService.delete(id);
    }
}
