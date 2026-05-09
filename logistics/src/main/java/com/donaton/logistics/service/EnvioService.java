package com.donaton.logistics.service;

import com.donaton.logistics.model.LogisticsEnvio;
import com.donaton.logistics.repository.EnvioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnvioService {

    private final EnvioRepository repository;

    public EnvioService(EnvioRepository repository) {
        this.repository = repository;
    }

    public LogisticsEnvio crearEnvio(LogisticsEnvio logisticsEnvio) {
        logisticsEnvio.setEstado("PENDIENTE");
        return repository.save(logisticsEnvio);
    }

    public List<LogisticsEnvio> listar() {
        return repository.findAll();
    }

    public LogisticsEnvio actualizarEstado(Long id, String estado) {
        LogisticsEnvio logisticsEnvio = repository.findById(id).orElseThrow();
        logisticsEnvio.setEstado(estado);
        return repository.save(logisticsEnvio);
    }
}