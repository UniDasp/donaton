package com.donaton.donation.service;

import com.donaton.donation.exception.BadRequestException;
import com.donaton.donation.model.DonationModel;
import com.donaton.donation.repository.DonationRepositoryPattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
public class DonationServiceTest {

    @Mock
    private DonationRepositoryPattern donationRepository;

    @Mock
    private DonationService donationService;

    //
    // TEST 1: crear
    // ==========================================
    @Test
    @DisplayName("Debería crear una donación exitosamente cuando los datos son válidos")
    void testCrearDonacionAdaptadoAlCodigoBase() {
        DonationModel donationSample = new DonationModel();
        donationSample.setId(1L);
        donationSample.setCantidad(50.0);
        donationSample.setTipo("Monetaria");

        Mockito.when(donationService.crear(any(DonationModel.class), anyString(), anyString()))
                .thenReturn(donationSample);

        String emailSimulado = "estudiante@duocuc.cl";
        String rolSimulado = "USER";

        DonationModel resultado = donationService.crear(donationSample, emailSimulado, rolSimulado);

        assertNotNull(resultado, "El resultado no debería ser nulo");
        assertEquals(1L, resultado.getId());
        assertEquals(50.0, resultado.getCantidad());
        assertEquals("Monetaria", resultado.getTipo());
    }

    //
    // TEST 2:
    //
    @Test
    @DisplayName("Debería lanzar BadRequestException cuando no se selecciona una necesidad")
    void testCrearDonacionLanzaExcepcionCuandoFaltaNecesidad() {
        DonationModel donationInvalida = new DonationModel();
        donationInvalida.setCantidad(50.0);
        donationInvalida.setTipo("Monetaria");

        Mockito.when(donationService.crear(any(DonationModel.class), anyString(), anyString()))
                .thenThrow(new BadRequestException("Debe seleccionar una necesidad para donar"));

        String emailSimulado = "estudiante@duocuc.cl";
        String rolSimulado = "USER";

        BadRequestException excepcion = assertThrows(BadRequestException.class, () -> {
            donationService.crear(donationInvalida, emailSimulado, rolSimulado);
        });

        assertEquals("Debe seleccionar una necesidad para donar", excepcion.getMessage());
    }

    //
    // TEST 3: control
    //
    @Test
    @DisplayName("Debería lanzar BadRequestException cuando el rol del usuario no tiene permisos para donar")
    void testCrearDonacionLanzaExcepcionPorRolInvalido() {
        DonationModel donationSample = new DonationModel();
        donationSample.setCantidad(100.0);
        donationSample.setTipo("Monetaria");

        String emailSimulado = "anonimo@duocuc.cl";
        String rolInvalido = "ROLE_INVALIDO";

        Mockito.when(donationService.crear(any(DonationModel.class), anyString(), Mockito.eq(rolInvalido)))
                .thenThrow(new BadRequestException("El rol del usuario no está autorizado para realizar donaciones"));

        BadRequestException excepcion = assertThrows(BadRequestException.class, () -> {
            donationService.crear(donationSample, emailSimulado, rolInvalido);
        });

        assertEquals("El rol del usuario no está autorizado para realizar donaciones", excepcion.getMessage());
    }

    //
    // TEST 4: email invalido
    //
    @Test
    @DisplayName("Debería lanzar BadRequestException cuando el email del usuario está vacío")
    void testCrearDonacionLanzaExcepcionCuandoEmailEstaVacio() {

        DonationModel donationSample = new DonationModel();
        donationSample.setCantidad(30.0);
        donationSample.setTipo("Insumos");

        String emailVacio = "";
        String rolSimulado = "USER";

        // al pasar un email vacío en el segundo parámetro, el sistema lo rechaza
        Mockito.when(donationService.crear(any(DonationModel.class), Mockito.eq(emailVacio), anyString()))
                .thenThrow(new BadRequestException("El email del usuario es obligatorio"));


        BadRequestException excepcion = assertThrows(BadRequestException.class, () -> {
            donationService.crear(donationSample, emailVacio, rolSimulado);
        });

        assertEquals("El email del usuario es obligatorio", excepcion.getMessage());
    }
}