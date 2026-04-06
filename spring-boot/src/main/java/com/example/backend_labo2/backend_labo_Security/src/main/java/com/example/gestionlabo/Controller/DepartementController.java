package com.example.gestionlabo.Controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.gestionlabo.Entites.Departement;
import com.example.gestionlabo.Service.DepartementService;

@RestController
@RequestMapping("/api/departements")
@CrossOrigin("*")
public class DepartementController {
	@Autowired
    private DepartementService departementService;
	//Create
	@PostMapping
	public Departement create(@RequestBody Departement d) {
		return departementService.create(d);
	}
	 // READ ALL
    @GetMapping
    public List<Departement> findAll() {
        return departementService.findAll();
    }

    // READ BY ID
    @GetMapping("/{id}")
    public Departement findById(@PathVariable Long id) {
        return departementService.findById(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public Departement update(@PathVariable Long id, @RequestBody Departement d) {
        return departementService.update(id, d);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        departementService.delete(id);
    }

}
