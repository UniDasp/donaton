package com.donaton.donation.service;

import com.donaton.donation.client.LogisticsClient;
import com.donaton.donation.dto.LogisticsRequestDTO;
import com.donaton.donation.exception.ResourceNotFoundException;
import com.donaton.donation.model.DonationModel;
import com.donaton.donation.repository.DonationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DonationService {

    private final DonationRepository repository;
    private final LogisticsClient logisticsClient;

    public DonationService(
            DonationRepository repository,
            LogisticsClient logisticsClient
    ) {
        this.repository = repository;
        this.logisticsClient = logisticsClient;
    }

    public DonationModel crear(DonationModel donation) {

        DonationModel nuevaDonacion =
                repository.save(donation);

        LogisticsRequestDTO logisticsRequest =
                new LogisticsRequestDTO(
                        nuevaDonacion.getId(),
                        nuevaDonacion.getDireccion(),
                        "PENDIENTE"
                );

        logisticsClient.crearEnvio(logisticsRequest);

        return nuevaDonacion;
    }

    public List<DonationModel> listar() {
        return repository.findAll();
    }

    public DonationModel buscarPorId(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Donación no encontrada"
                        )
                );
    }

    public DonationModel actualizar(Long id, DonationModel donation) {
        return donation;
    }

    public void eliminar(Long id) {
    }
}