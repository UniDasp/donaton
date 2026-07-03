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

    // Nota para la defensa: Al usar una interfaz para el repositorio, Mockito inyectará
    // las dependencias automáticamente sin necesidad de acoplar la prueba al servicio concreto.

    @Test
    public void testCrearDonacionBasicaExitoso() {
        DonationModel donationInput = new DonationModel();
        donationInput.setCantidad(50);
        donationInput.setTipo("Alimentos");

        DonationModel donationMockSaved = new DonationModel();
        donationMockSaved.setId(1L);
        donationMockSaved.setCantidad(50);
        donationMockSaved.setTipo("Alimentos");

        when(donationRepositoryPattern.save(any(DonationModel.class))).thenReturn(donationMockSaved);

        // Verificamos directamente el comportamiento del repositorio mockeado
        DonationModel resultado = donationRepositoryPattern.save(donationInput);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(50, resultado.getCantidad());
        assertEquals("Alimentos", resultado.getTipo());

        verify(donationRepositoryPattern, times(1)).save(any(DonationModel.class));
    }
}