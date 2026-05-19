package com.donaton.donation.controller;

import com.donaton.donation.dto.DonationRequestDTO;
import com.donaton.donation.dto.DonationResponseDTO;
import com.donaton.donation.mapper.DonationMapper;
import com.donaton.donation.service.DonationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    @ResponseStatus(HttpStatus.CREATED)
    public DonationResponseDTO crear(
            @Valid @RequestBody DonationRequestDTO request,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role") String role
    ) {
        return DonationMapper.toResponse(
                service.crear(DonationMapper.toModel(request), email, role)
        );
    }

    @GetMapping
    public List<DonationResponseDTO> listar() {
        return service.listar().stream().map(DonationMapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public DonationResponseDTO buscar(@PathVariable Long id) {
        return DonationMapper.toResponse(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public DonationResponseDTO actualizar(
            @PathVariable Long id,
            @Valid @RequestBody DonationRequestDTO request
    ) {
        return DonationMapper.toResponse(
                service.actualizar(id, DonationMapper.toModel(request))
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
