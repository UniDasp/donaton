package com.donaton.donation;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.donaton.donation.model.DonationModel;
import com.donaton.donation.repository.DonationRepositoryPattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DonationServiceTest {

    @Mock
    private DonationRepositoryPattern donationRepositoryPattern;

    @Test
    public void testCrearDonacionBasicaExitoso() {
        DonationModel donationInput = new DonationModel();
        donationInput.setCantidad(50.0);
        donationInput.setTipo("Alimentos");

        DonationModel donationMockSaved = new DonationModel();
        donationMockSaved.setId(1L);
        donationMockSaved.setCantidad(50.0);
        donationMockSaved.setTipo("Alimentos");

        when(donationRepositoryPattern.save(any(DonationModel.class))).thenReturn(donationMockSaved);

        DonationModel resultado = donationRepositoryPattern.save(donationInput);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(50.0, resultado.getCantidad());
        assertEquals("Alimentos", resultado.getTipo());

        verify(donationRepositoryPattern, times(1)).save(any(DonationModel.class));
    }


    @Test
    public void testCrearDonacionConCantidadInvalidaDebeFallar() {
        DonationModel donationInvalida = new DonationModel();
        donationInvalida.setCantidad(-15.0);
        donationInvalida.setTipo("Medicamentos");

        when(donationRepositoryPattern.save(argThat(argument -> argument.getCantidad() <= 0)))
                .thenThrow(new IllegalArgumentException("La cantidad de la donación debe ser mayor a cero"));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            donationRepositoryPattern.save(donationInvalida);
        });

        assertEquals("La cantidad de la donación debe ser mayor a cero", exception.getMessage());
        verify(donationRepositoryPattern, times(1)).save(donationInvalida);
    }


    @Test
    public void testCrearDonacionConTipoVacioDebeFallar() {
        DonationModel donationSinTipo = new DonationModel();
        donationSinTipo.setCantidad(100.0);
        donationSinTipo.setTipo("");

        when(donationRepositoryPattern.save(argThat(argument -> argument.getTipo() == null || argument.getTipo().isEmpty())))
                .thenThrow(new IllegalArgumentException("El tipo de donación no puede estar vacío"));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            donationRepositoryPattern.save(donationSinTipo);
        });

        assertEquals("El tipo de donación no puede estar vacío", exception.getMessage());
        verify(donationRepositoryPattern, times(1)).save(donationSinTipo);
    }
}