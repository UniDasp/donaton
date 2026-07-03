package com.donaton.donation;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.donaton.donation.model.DonationModel;
import com.donaton.donation.repository.DonationRepositoryPattern;
import com.donaton.donation.service.DonationService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DonationServiceTest {

    @Mock
    private DonationRepositoryPattern donationRepositoryPattern; // Tu repositorio real

    @InjectMocks
    private DonationService donationService; // Tu servicio real

    @Test
    public void testCrearDonacionBasicaExitoso() {
        // ARRANGE (Preparar los datos de prueba simulados)
        DonationModel donationInput = new DonationModel();
        donationInput.setCantidad(50);         // Satisface la validación: cantidad > 0
        donationInput.setTipo("Alimentos");    // Satisface la validación: tipo no vacío
        // Nota: Dejamos needId en null para que no intente llamar a los clientes Feign (needsClient) en esta prueba básica

        DonationModel donationMockSaved = new DonationModel();
        donationMockSaved.setId(1L);
        donationMockSaved.setCantidad(50);
        donationMockSaved.setTipo("Alimentos");

        // Simulamos que cuando el repositorio guarde cualquier objeto DonationModel, devuelva nuestro objeto con ID asignado
        when(donationRepositoryPattern.save(any(DonationModel.class))).thenReturn(donationMockSaved);

        // ACT (Ejecutar el método real "crear" pasándole los datos requeridos)
        DonationModel resultado = donationService.crear(donationInput, "test@donaton.com", "ADMIN");

        // ASSERT (Verificar que todo se haya procesado de forma correcta)
        assertNotNull(resultado, "El resultado no debería ser nulo");
        assertEquals(1L, resultado.getId(), "El ID debería ser 1L");
        assertEquals(50, resultado.getCantidad());
        assertEquals("Alimentos", resultado.getTipo());

        // Verificar que el repositorio guardó los datos exactamente 1 vez
        verify(donationRepositoryPattern, times(1)).save(any(DonationModel.class));
    }
}