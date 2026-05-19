package com.donaton.donation.controller;

import com.donaton.donation.model.DonationModel;
import com.donaton.donation.service.DonationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/donations")
public class DonationController {

    private final DonationService service;

    public DonationController(DonationService service) {
        this.service = service;
    }

    @PostMapping
    public DonationModel crear(
            @RequestBody DonationModel donation,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role") String role
    ) {
        return service.crear(donation, email, role);
    }

    @GetMapping
    public List<DonationModel> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public DonationModel buscar(
            @PathVariable Long id
    ) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public DonationModel actualizar(
            @PathVariable Long id,
            @RequestBody DonationModel donation
    ) {
        return service.actualizar(id, donation);
    }

    @DeleteMapping("/{id}")
    public void eliminar(
            @PathVariable Long id
    ) {
        service.eliminar(id);
    }








}