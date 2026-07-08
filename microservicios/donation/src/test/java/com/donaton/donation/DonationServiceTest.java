package com.donaton.donation;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.donaton.donation.client.LogisticsClient;
import com.donaton.donation.client.NeedsClient;
import com.donaton.donation.dto.NeedDTO;
import com.donaton.donation.exception.BadRequestException;
import com.donaton.donation.exception.ResourceNotFoundException;
import com.donaton.donation.model.DonationModel;
import com.donaton.donation.repository.DonationRepositoryPattern;
import com.donaton.donation.service.DonationService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class DonationServiceTest {

    @Mock
    private DonationRepositoryPattern repository;

    @Mock
    private NeedsClient needsClient;

    @Mock
    private LogisticsClient logisticsClient;

    @InjectMocks
    private DonationService donationService;

    @Test
    public void testCrearDonacionBasicaExitoso() {
        DonationModel donationInput = new DonationModel();
        donationInput.setCantidad(50.0);
        donationInput.setTipo("Alimentos");

        DonationModel donationMockSaved = new DonationModel();
        donationMockSaved.setId(1L);
        donationMockSaved.setCantidad(50.0);
        donationMockSaved.setTipo("Alimentos");

        when(repository.save(any(DonationModel.class))).thenReturn(donationMockSaved);

        DonationModel resultado = donationService.crear(donationInput, "user@test.com", "ADMIN");

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(50.0, resultado.getCantidad());
        assertEquals("Alimentos", resultado.getTipo());

        verify(repository, times(1)).save(any(DonationModel.class));
    }

    @Test
    public void testCrearDonacionConCantidadInvalidaDebeFallar() {
        DonationModel donationInvalida = new DonationModel();
        donationInvalida.setCantidad(-15.0);
        donationInvalida.setTipo("Medicamentos");

        Exception exception = assertThrows(BadRequestException.class, () -> {
            donationService.crear(donationInvalida, "user@test.com", "ADMIN");
        });

        assertEquals("Cantidad inválida", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    public void testCrearDonacionConTipoVacioDebeFallar() {
        DonationModel donationSinTipo = new DonationModel();
        donationSinTipo.setCantidad(100.0);
        donationSinTipo.setTipo("");

        Exception exception = assertThrows(BadRequestException.class, () -> {
            donationService.crear(donationSinTipo, "user@test.com", "ADMIN");
        });

        assertEquals("Tipo inválido", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    public void testCrearDonacionConRolInvalidoDebeFallar() {
        DonationModel donation = new DonationModel();
        donation.setCantidad(100.0);
        donation.setTipo("Alimentos");

        Exception exception = assertThrows(BadRequestException.class, () -> {
            donationService.crear(donation, "user@test.com", "");
        });

        assertEquals("Rol inválido", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    public void testCrearDonacionUserSinNeedIdDebeFallar() {
        DonationModel donation = new DonationModel();
        donation.setCantidad(100.0);
        donation.setTipo("Alimentos");

        Exception exception = assertThrows(BadRequestException.class, () -> {
            donationService.crear(donation, "user@test.com", "USER");
        });

        assertEquals("Debe seleccionar una necesidad para donar", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    public void testListarDonaciones() {
        DonationModel donation1 = new DonationModel();
        donation1.setId(1L);
        donation1.setCantidad(50.0);

        DonationModel donation2 = new DonationModel();
        donation2.setId(2L);
        donation2.setCantidad(100.0);

        when(repository.findAll()).thenReturn(List.of(donation1, donation2));

        var resultado = donationService.listar();

        assertEquals(2, resultado.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    public void testBuscarPorIdExitoso() {
        DonationModel donation = new DonationModel();
        donation.setId(1L);
        donation.setCantidad(50.0);

        when(repository.findById(1L)).thenReturn(java.util.Optional.of(donation));

        DonationModel resultado = donationService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    public void testBuscarPorIdNoEncontrado() {
        when(repository.findById(999L)).thenReturn(java.util.Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            donationService.buscarPorId(999L);
        });

        assertEquals("Donación no encontrada", exception.getMessage());
    }

    @Test
    public void testActualizarDonacion() {
        DonationModel existente = new DonationModel();
        existente.setId(1L);
        existente.setCantidad(50.0);
        existente.setTipo("Alimentos");

        DonationModel actualizacion = new DonationModel();
        actualizacion.setCantidad(100.0);
        actualizacion.setTipo("Medicamentos");

        when(repository.findById(1L)).thenReturn(java.util.Optional.of(existente));
        when(repository.save(any(DonationModel.class))).thenReturn(existente);

        DonationModel resultado = donationService.actualizar(1L, actualizacion);

        assertNotNull(resultado);
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(any(DonationModel.class));
    }

    @Test
    public void testEliminarDonacion() {
        DonationModel existente = new DonationModel();
        existente.setId(1L);

        when(repository.findById(1L)).thenReturn(java.util.Optional.of(existente));
        doNothing().when(repository).delete(any());

        donationService.eliminar(1L);

        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).delete(existente);
    }
}