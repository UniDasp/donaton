package com.donaton.logistics.controller;

import com.donaton.logistics.dto.EnvioRequestDTO;
import com.donaton.logistics.model.LogisticsEnvio;
import com.donaton.logistics.service.EnvioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/envios")
public class EnvioController {

    private final EnvioService service;

    public EnvioController(EnvioService service) {
        this.service = service;
    }

    @PostMapping
    public LogisticsEnvio crear(
            @RequestBody EnvioRequestDTO request,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role") String role
    ) {
        return service.crearEnvio(request.getDonacionId(), email, role);
    }

    @GetMapping
    public List<LogisticsEnvio> listar(
            @RequestParam(required = false) String acopioCenterId
    ) {
        return service.listar(acopioCenterId);
    }

    @PutMapping("/{id}/estado")
    public LogisticsEnvio actualizarEstado(
            @PathVariable Long id,
            @RequestParam String estado,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role") String role
    ) {
        return service.actualizarEstado(id, estado, email, role);
    }
}
