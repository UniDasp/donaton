package com.donaton.needs;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.donaton.needs.model.NeedEntity;
import com.donaton.needs.repository.NeedRepositoryPattern;
import com.donaton.needs.service.NeedService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class NeedServiceTest {

    @Mock
    private NeedRepositoryPattern repository;

    @InjectMocks
    private NeedService needService;

    @Test
    public void testListarTodasLasNecesidades() {
        NeedEntity need1 = new NeedEntity();
        need1.setId("need1");

        NeedEntity need2 = new NeedEntity();
        need2.setId("need2");

        when(repository.findAll()).thenReturn(List.of(need1, need2));

        List<NeedEntity> resultado = needService.list(null, null);

        assertEquals(2, resultado.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    public void testListarPorCategoria() {
        NeedEntity need = new NeedEntity();
        need.setId("need1");
        need.setCategory("Alimentos");

        when(repository.findByCategory("Alimentos")).thenReturn(List.of(need));

        List<NeedEntity> resultado = needService.list("Alimentos", null);

        assertEquals(1, resultado.size());
        verify(repository, times(1)).findByCategory("Alimentos");
    }

    @Test
    public void testListarPorStatus() {
        NeedEntity need = new NeedEntity();
        need.setId("need1");
        need.setStatus("en_proceso");

        when(repository.findByStatus("en_proceso")).thenReturn(List.of(need));

        List<NeedEntity> resultado = needService.list(null, "en_proceso");

        assertEquals(1, resultado.size());
        verify(repository, times(1)).findByStatus("en_proceso");
    }

    @Test
    public void testListarPorCategoriaYStatus() {
        NeedEntity need = new NeedEntity();
        need.setId("need1");
        need.setCategory("Alimentos");
        need.setStatus("en_proceso");

        when(repository.findByCategoryAndStatus("Alimentos", "en_proceso")).thenReturn(List.of(need));

        List<NeedEntity> resultado = needService.list("Alimentos", "en_proceso");

        assertEquals(1, resultado.size());
        verify(repository, times(1)).findByCategoryAndStatus("Alimentos", "en_proceso");
    }

    @Test
    public void testGetByIdExitoso() {
        NeedEntity need = new NeedEntity();
        need.setId("need123");

        when(repository.findById("need123")).thenReturn(Optional.of(need));

        NeedEntity resultado = needService.getById("need123");

        assertNotNull(resultado);
        assertEquals("need123", resultado.getId());
    }

    @Test
    public void testGetByIdNoEncontradoDebeFallar() {
        when(repository.findById("need999")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            needService.getById("need999");
        });

        assertEquals("Need not found", exception.getMessage());
    }

    @Test
    public void testCreateExitoso() {
        NeedEntity need = new NeedEntity();
        need.setId("need123");
        need.setAddress("Calle 123");
        need.setQuantityRequired(100.0);

        when(repository.save(any(NeedEntity.class))).thenReturn(need);

        NeedEntity resultado = needService.create(need, "user@test.com");

        assertNotNull(resultado);
        assertEquals("user@test.com", resultado.getCreatedByEmail());
        assertEquals(0.0, resultado.getQuantityReceived());
        assertEquals(0, resultado.getMatchedDonations());
    }

    @Test
    public void testCreateAddressVacioDebeFallar() {
        NeedEntity need = new NeedEntity();
        need.setAddress("");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            needService.create(need, "user@test.com");
        });

        assertEquals("Address is required", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    public void testUpdateExitoso() {
        NeedEntity existente = new NeedEntity();
        existente.setId("need123");
        existente.setAddress("Calle Vieja");

        NeedEntity actualizacion = new NeedEntity();
        actualizacion.setAddress("Calle Nueva");
        actualizacion.setCategory("Medicamentos");

        when(repository.findById("need123")).thenReturn(Optional.of(existente));
        when(repository.save(any(NeedEntity.class))).thenReturn(existente);

        NeedEntity resultado = needService.update("need123", actualizacion);

        assertNotNull(resultado);
        verify(repository, times(1)).save(any(NeedEntity.class));
    }

    @Test
    public void testUpdateAddressVacioDebeFallar() {
        NeedEntity existente = new NeedEntity();
        existente.setId("need123");
        existente.setAddress("Calle Vieja");

        NeedEntity actualizacion = new NeedEntity();
        actualizacion.setAddress("");

        when(repository.findById("need123")).thenReturn(Optional.of(existente));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            needService.update("need123", actualizacion);
        });

        assertEquals("Address is required", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    public void testReceiveExitoso() {
        NeedEntity need = new NeedEntity();
        need.setId("need123");
        need.setQuantityRequired(100.0);
        need.setQuantityReceived(50.0);

        when(repository.findById("need123")).thenReturn(Optional.of(need));
        when(repository.save(any(NeedEntity.class))).thenReturn(need);

        NeedEntity resultado = needService.receive("need123", 30.0);

        assertNotNull(resultado);
        assertEquals(80.0, resultado.getQuantityReceived());
        assertEquals(1, resultado.getMatchedDonations());
    }

    @Test
    public void testReceiveCantidadInvalidaDebeFallar() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            needService.receive("need123", -10.0);
        });

        assertEquals("Invalid amount", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    public void testReceiveMarcaComoSatisfecha() {
        NeedEntity need = new NeedEntity();
        need.setId("need123");
        need.setQuantityRequired(100.0);
        need.setQuantityReceived(90.0);

        when(repository.findById("need123")).thenReturn(Optional.of(need));
        when(repository.save(any(NeedEntity.class))).thenReturn(need);

        NeedEntity resultado = needService.receive("need123", 15.0);

        assertEquals("satisfecha", resultado.getStatus());
    }

    @Test
    public void testRollbackReceiveExitoso() {
        NeedEntity need = new NeedEntity();
        need.setId("need123");
        need.setQuantityReceived(80.0);
        need.setMatchedDonations(5);
        need.setStatus("satisfecha");
        need.setQuantityRequired(100.0);

        when(repository.findById("need123")).thenReturn(Optional.of(need));
        when(repository.save(any(NeedEntity.class))).thenReturn(need);

        NeedEntity resultado = needService.rollbackReceive("need123", 30.0);

        assertEquals(50.0, resultado.getQuantityReceived());
        assertEquals(4, resultado.getMatchedDonations());
        assertEquals("en_proceso", resultado.getStatus());
    }

    @Test
    public void testRollbackReceiveCantidadInvalidaDebeFallar() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            needService.rollbackReceive("need123", -10.0);
        });

        assertEquals("Invalid amount", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    public void testDeleteExitoso() {
        doNothing().when(repository).deleteById("need123");

        needService.delete("need123");

        verify(repository, times(1)).deleteById("need123");
    }
}
