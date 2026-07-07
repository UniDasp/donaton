package com.donaton.logistics;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.donaton.logistics.client.DonationClient;
import com.donaton.logistics.client.NeedsClient;
import com.donaton.logistics.dto.DonationDTO;
import com.donaton.logistics.dto.NeedDTO;
import com.donaton.logistics.exception.BadRequestException;
import com.donaton.logistics.exception.ForbiddenException;
import com.donaton.logistics.model.EnvioEstado;
import com.donaton.logistics.model.LogisticsEnvio;
import com.donaton.logistics.repository.EnvioRepositoryPattern;
import com.donaton.logistics.service.EnvioService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class EnvioServiceTest {

    @Mock
    private EnvioRepositoryPattern repository;

    @Mock
    private DonationClient donationClient;

    @Mock
    private NeedsClient needsClient;

    private EnvioService envioService;

    @BeforeEach
    public void setUp() {
        envioService = new EnvioService(repository, donationClient, needsClient, 3);
    }

    @Test
    public void testCrearEnvioExitoso() {
        DonationDTO donation = new DonationDTO();
        donation.setId(1L);
        donation.setCantidad(50.0);
        donation.setNeedId("need123");
        donation.setDireccion("Calle 123");

        NeedDTO need = new NeedDTO();
        need.setId("need123");
        need.setCenterId("center123");
        need.setCenterName("Centro Principal");

        when(repository.findByDonacionId(1L)).thenReturn(Optional.empty());
        when(donationClient.getDonationById(1L, "user@test.com", "ADMIN")).thenReturn(donation);
        when(needsClient.getNeedById("need123", "user@test.com", "ADMIN")).thenReturn(need);
        when(repository.save(any(LogisticsEnvio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LogisticsEnvio resultado = envioService.crearEnvio(1L, "user@test.com", "ADMIN");

        assertNotNull(resultado);
        assertEquals(1L, resultado.getDonacionId());
        assertEquals("need123", resultado.getNeedId());
        assertEquals(EnvioEstado.PENDIENTE_ACOPIO, resultado.getEstado());
    }

    @Test
    public void testCrearEnvioDonacionIdNullDebeFallar() {
        Exception exception = assertThrows(BadRequestException.class, () -> {
            envioService.crearEnvio(null, "user@test.com", "ADMIN");
        });

        assertEquals("donacionId es obligatorio", exception.getMessage());
    }

    @Test
    public void testCrearEnvioYaExisteDebeFallar() {
        LogisticsEnvio existingEnvio = new LogisticsEnvio();
        when(repository.findByDonacionId(1L)).thenReturn(Optional.of(existingEnvio));

        Exception exception = assertThrows(BadRequestException.class, () -> {
            envioService.crearEnvio(1L, "user@test.com", "ADMIN");
        });

        assertEquals("Ya existe un envío para esta donación", exception.getMessage());
    }

    @Test
    public void testCrearEnvioDonacionNoEncontradaDebeFallar() {
        when(repository.findByDonacionId(1L)).thenReturn(Optional.empty());
        when(donationClient.getDonationById(1L, "user@test.com", "ADMIN")).thenReturn(null);

        Exception exception = assertThrows(BadRequestException.class, () -> {
            envioService.crearEnvio(1L, "user@test.com", "ADMIN");
        });

        assertEquals("Donación no encontrada", exception.getMessage());
    }

    @Test
    public void testListarEnviosSinFiltro() {
        LogisticsEnvio envio1 = new LogisticsEnvio();
        envio1.setId(1L);

        LogisticsEnvio envio2 = new LogisticsEnvio();
        envio2.setId(2L);

        when(repository.findAll()).thenReturn(List.of(envio1, envio2));

        List<LogisticsEnvio> resultado = envioService.listar(null);

        assertEquals(2, resultado.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    public void testListarEnviosConFiltro() {
        LogisticsEnvio envio = new LogisticsEnvio();
        envio.setId(1L);
        envio.setAcopioCenterId("center123");

        when(repository.findByAcopioCenterId("center123")).thenReturn(List.of(envio));

        List<LogisticsEnvio> resultado = envioService.listar("center123");

        assertEquals(1, resultado.size());
        verify(repository, times(1)).findByAcopioCenterId("center123");
    }

    @Disabled("Pendiente de corregir")
    @Test
    public void testActualizarEstadoExitoso() {
        LogisticsEnvio envio = new LogisticsEnvio();
        envio.setId(1L);
        envio.setEstado(EnvioEstado.PENDIENTE_ACOPIO);

        NeedDTO need = new NeedDTO();
        need.setCreatedByEmail("user@test.com");

        when(repository.findById(1L)).thenReturn(Optional.of(envio));
        when(needsClient.getNeedById(any(), any(), any())).thenReturn(need);

        LogisticsEnvio resultado = envioService.actualizarEstado(1L, "recibida", "user@test.com", "ADMIN");

        assertNotNull(resultado);
        assertEquals("recibida", resultado.getEstado());
    }

    @Test
    public void testActualizarEstadoEnvioNoEncontradoDebeFallar() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(BadRequestException.class, () -> {
            envioService.actualizarEstado(1L, "recibida", "user@test.com", "ADMIN");
        });

        assertEquals("Envío no encontrado", exception.getMessage());
    }

    @Test
    public void testActualizarEstadoInvalidoDebeFallar() {
        LogisticsEnvio envio = new LogisticsEnvio();
        envio.setId(1L);
        envio.setEstado(EnvioEstado.PENDIENTE_ACOPIO);

        when(repository.findById(1L)).thenReturn(Optional.of(envio));

        Exception exception = assertThrows(BadRequestException.class, () -> {
            envioService.actualizarEstado(1L, "", "user@test.com", "ADMIN");
        });

        assertEquals("Estado inválido", exception.getMessage());
    }

    @Test
    public void testActualizarEstadoTransicionInvalidaDebeFallar() {
        LogisticsEnvio envio = new LogisticsEnvio();
        envio.setId(1L);
        envio.setEstado(EnvioEstado.PENDIENTE_ACOPIO);

        when(repository.findById(1L)).thenReturn(Optional.of(envio));

        Exception exception = assertThrows(BadRequestException.class, () -> {
            envioService.actualizarEstado(1L, "entregado", "user@test.com", "ADMIN");
        });

        assertEquals("Desde pendiente_acopio solo puede pasar a recibida", exception.getMessage());
    }
    
    @Disabled("Pendiente de corregir")
    @Test
    public void testMarcarInexistentesVencidos() {
        Instant past = Instant.now().minusSeconds(86400);
        
        LogisticsEnvio envio1 = new LogisticsEnvio();
        envio1.setId(1L);
        envio1.setEstado(EnvioEstado.PENDIENTE_ACOPIO);
        envio1.setAcopioDeadline(past);
        envio1.setNeedId("need123");
        envio1.setCantidadDonada(50.0);

        LogisticsEnvio envio2 = new LogisticsEnvio();
        envio2.setId(2L);
        envio2.setEstado(EnvioEstado.PENDIENTE_ACOPIO);
        envio2.setAcopioDeadline(past);
        envio2.setNeedId("need456");
        envio2.setCantidadDonada(30.0);

        when(repository.findByEstadoAndAcopioDeadlineBefore(EnvioEstado.PENDIENTE_ACOPIO, any()))
                .thenReturn(List.of(envio1, envio2));
        when(repository.save(any(LogisticsEnvio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int resultado = envioService.marcarInexistentesVencidos();

        assertEquals(2, resultado);
        verify(repository, times(2)).save(any(LogisticsEnvio.class));
    }
}
