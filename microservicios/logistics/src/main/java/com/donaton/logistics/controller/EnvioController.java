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
            @RequestBody EnvioRequestDTO request
    ) {

        LogisticsEnvio envio = new LogisticsEnvio();

        envio.setDonacionId(request.getDonacionId());
        envio.setDireccion(request.getDireccion());
        envio.setEstado(request.getEstado());

        return service.crearEnvio(envio);
    }

    @GetMapping
    public List<LogisticsEnvio> listar() {
        return service.listar();
    }

    @PutMapping("/{id}/estado")
    public LogisticsEnvio actualizarEstado(
            @PathVariable Long id,
            @RequestParam String estado
    ) {
        return service.actualizarEstado(id, estado);
    }
}