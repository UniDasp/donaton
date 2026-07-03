package com.donaton.donation;

import com.donaton.donation.model.DonationModel;
import com.donaton.donation.repository.DonationRepository;
import com.donaton.donation.service.DonationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DonationServiceTest {

    @Mock
    private DonationRepository donationRepository;

    @InjectMocks
    private DonationService donationService;

    @Test
    public void testRegistrarDonacionBasico() {
        // ARRANGE (Preparar el escenario simulado)
        DonationModel donationMock = new DonationModel();
        donationMock.setId(1L);

        // Simulamos el comportamiento del repositorio usando la clase correcta (DonationModel)
        when(donationRepository.save(any(DonationModel.class))).thenReturn(donationMock);

        // ACT (Ejecutar el método del servicio)
        // ⚠️ IMPORTANTE: Si tu método en DonationService se llama distinto a "registrar" (por ejemplo, "save" o "create"), cámbialo aquí abajo.
        DonationModel resultado = donationService.registrar(new DonationModel());

        // ASSERT (Verificar los resultados obtenidos)
        assertNotNull(resultado, "El objeto resultado no debería ser nulo");
        assertEquals(1L, resultado.getId(), "El ID debería coincidir con el valor mockeado (1L)");

        // Verificar que el repositorio interactuó exactamente 1 vez
        verify(donationRepository, times(1)).save(any(DonationModel.class));
    }
}