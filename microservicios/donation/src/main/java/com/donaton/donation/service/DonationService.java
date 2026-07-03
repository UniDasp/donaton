package com.donaton.donation.service;

import com.donaton.donation.model.DonationModel;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class DonationService {

    public DonationModel crear(DonationModel donation, String email, String role) {
        return donation;
    }

    public List<DonationModel> listar() {
        return new ArrayList<>();
    }

    public DonationModel buscarPorId(Long id) {
        return new DonationModel();
    }

    public DonationModel actualizar(Long id, DonationModel donation) {
        return donation;
    }

    public void eliminar(Long id) {
        // Lógica de eliminación simulada
    }
}