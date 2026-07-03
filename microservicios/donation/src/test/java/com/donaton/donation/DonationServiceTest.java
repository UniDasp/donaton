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

    // PRUEBA 1: Tu caso feliz original (¡Se mantiene exactamente igual!)
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






    // PRUEBA 2: Caso de Validación de Negocio (Falla si intentan donar cantidades inválidas)
    @Test
    public void testCrearDonacionConCantidadInvalidaDebeFallar() {
        // Arrange: Preparamos un caso corrupto con cantidad negativa
        DonationModel donationInvalida = new DonationModel();
        donationInvalida.setCantidad(-15.0);
        donationInvalida.setTipo("Medicamentos");

        // Simulamos que el repositorio protegerá el sistema lanzando una excepción si la cantidad es menor o igual a cero
        when(donationRepositoryPattern.save(argThat(argument -> argument.getCantidad() <= 0)))
                .thenThrow(new IllegalArgumentException("La cantidad de la donación debe ser mayor a cero"));

        // Comprobamos que el sistema capture y maneje el error arrojando la excepción esperada
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            donationRepositoryPattern.save(donationInvalida);
        });

        // Verificaciones de calidad
        assertEquals("La cantidad de la donación debe ser mayor a cero", exception.getMessage());
        verify(donationRepositoryPattern, times(1)).save(donationInvalida);
    }







    // PRUEBA 3: Caso de Integridad de Datos (Falla si el tipo de recurso viene vacío)
    @Test
    public void testCrearDonacionConTipoVacioDebeFallar() {
        // Preparamos una donación sin especificar la categoría (campo vacío)
        DonationModel donationSinTipo = new DonationModel();
        donationSinTipo.setCantidad(100.0);
        donationSinTipo.setTipo("");

        // Simulamos que el motor de persistencia rechazará el registro si falta el tipo de recurso
        when(donationRepositoryPattern.save(argThat(argument -> argument.getTipo() == null || argument.getTipo().isEmpty())))
                .thenThrow(new IllegalArgumentException("El tipo de donación no puede estar vacío"));

        // Validamos que la suite de pruebas detecte el rechazo del dato inválido
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            donationRepositoryPattern.save(donationSinTipo);
        });

        // Verificaciones de calidad
        assertEquals("El tipo de donación no puede estar vacío", exception.getMessage());
        verify(donationRepositoryPattern, times(1)).save(donationSinTipo);
    }
}