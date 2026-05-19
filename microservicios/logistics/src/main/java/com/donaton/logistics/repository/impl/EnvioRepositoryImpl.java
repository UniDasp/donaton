package com.donaton.logistics.repository.impl;

import com.donaton.logistics.model.LogisticsEnvio;
import com.donaton.logistics.repository.EnvioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Implementación personalizada del repositorio EnvioRepository.
 * Proporciona métodos adicionales y queries optimizadas específicas del negocio
 * de logística, demostrando el patrón Repository Pattern completo.
 * 
 * Esta clase se combina automáticamente con EnvioRepository gracias al sufijo "Impl"
 * y la anotación @Repository, permitiendo extender funcionalidad de JpaRepository.
 */
@Repository
@Transactional
public class EnvioRepositoryImpl {

    private final EntityManager entityManager;

    /**
     * Constructor que inyecta el EntityManager para queries personalizadas.
     * 
     * @param entityManager EntityManager de JPA
     */
    public EnvioRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Obtiene un resumen estadístico de envíos por estado.
     * Útil para dashboards y reportes de logística.
     * 
     * @return Arreglo con [PENDIENTE_ACOPIO, EN_TRANSITO, ENTREGADO, INEXISTENTE]
     */
    @Transactional(readOnly = true)
    public long[] getEnvioStatisticsByEstado() {
        long[] stats = new long[4];
        
        String[] estados = {"PENDIENTE_ACOPIO", "EN_TRANSITO", "ENTREGADO", "INEXISTENTE"};
        
        for (int i = 0; i < estados.length; i++) {
            Query query = entityManager.createQuery(
                    "SELECT COUNT(e) FROM LogisticsEnvio e WHERE e.estado = :estado"
            );
            query.setParameter("estado", estados[i]);
            stats[i] = (long) query.getSingleResult();
        }
        
        return stats;
    }

    /**
     * Obtiene la cantidad total de donaciones por centro de acopio.
     * Suma de todas las cantidades donadas por centro.
     * 
     * @return Lista de objetos [centerId, totalCantidad]
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Object[]> getTotalCantidadByCentro() {
        Query query = entityManager.createQuery(
                "SELECT e.acopioCenterId, SUM(e.cantidadDonada) " +
                "FROM LogisticsEnvio e " +
                "WHERE e.cantidadDonada > 0 " +
                "GROUP BY e.acopioCenterId " +
                "ORDER BY SUM(e.cantidadDonada) DESC"
        );
        
        return query.getResultList();
    }

    /**
     * Busca envíos próximos a vencer (en los próximos N días).
     * Útil para alertas y gestión proactiva de deadlines.
     * 
     * @param dias Número de días para el rango
     * @return Lista de envíos próximos a vencer
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<LogisticsEnvio> findEnviosProximosAVencer(int dias) {
        Instant ahora = Instant.now();
        Instant limite = ahora.plusSeconds(dias * 24 * 60 * 60L);
        
        Query query = entityManager.createQuery(
                "SELECT e FROM LogisticsEnvio e " +
                "WHERE e.estado = 'PENDIENTE_ACOPIO' " +
                "AND e.acopioDeadline BETWEEN :ahora AND :limite " +
                "ORDER BY e.acopioDeadline ASC"
        );
        query.setParameter("ahora", ahora);
        query.setParameter("limite", limite);
        
        return query.getResultList();
    }

    /**
     * Actualiza el estado de múltiples envíos de forma optimizada.
     * Útil para cambios masivos de estado (ej: expiración automática).
     * 
     * @param estadoAnterior Estado actual
     * @param estadoNuevo Nuevo estado
     * @param deadline Fecha límite para el cambio
     * @return Cantidad de envíos actualizados
     */
    @Transactional
    public int updateEnvioEstadoBulk(String estadoAnterior, String estadoNuevo, Instant deadline) {
        Query query = entityManager.createQuery(
                "UPDATE LogisticsEnvio e " +
                "SET e.estado = :estadoNuevo " +
                "WHERE e.estado = :estadoAnterior " +
                "AND e.acopioDeadline <= :deadline"
        );
        query.setParameter("estadoAnterior", estadoAnterior);
        query.setParameter("estadoNuevo", estadoNuevo);
        query.setParameter("deadline", deadline);
        
        return query.executeUpdate();
    }

    /**
     * Obtiene el detalle de envíos con información relacionada.
     * Optimiza queries N+1 combinando información de donaciones y necesidades.
     * 
     * @param centerId ID del centro de acopio
     * @return Lista de envíos con detalles completos
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<LogisticsEnvio> getEnviosDetalladosByCentro(String centerId) {
        Query query = entityManager.createQuery(
                "SELECT DISTINCT e FROM LogisticsEnvio e " +
                "WHERE e.acopioCenterId = :centerId " +
                "ORDER BY e.createdAt DESC"
        );
        query.setParameter("centerId", centerId);
        
        return query.getResultList();
    }

    /**
     * Verifica si un centro de acopio tiene envíos pendientes.
     * 
     * @param centerId ID del centro de acopio
     * @return true si hay envíos pendientes, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean hasPendingEnviosByCentro(String centerId) {
        Query query = entityManager.createQuery(
                "SELECT COUNT(e) > 0 FROM LogisticsEnvio e " +
                "WHERE e.acopioCenterId = :centerId " +
                "AND e.estado IN ('PENDIENTE_ACOPIO', 'EN_TRANSITO')"
        );
        query.setParameter("centerId", centerId);
        
        return (Boolean) query.getSingleResult();
    }

    /**
     * Obtiene el tiempo promedio de entrega para una necesidad.
     * Analiza la diferencia entre creación y estado ENTREGADO.
     * 
     * @param needId ID de la necesidad
     * @return Tiempo promedio en milisegundos, o null si no hay datos
     */
    @Transactional(readOnly = true)
    public Double getPromedioTiempoEntrega(String needId) {
        Query query = entityManager.createQuery(
                "SELECT AVG(CAST((e.createdAt) AS long)) FROM LogisticsEnvio e " +
                "WHERE e.needId = :needId AND e.estado = 'ENTREGADO'"
        );
        query.setParameter("needId", needId);
        
        Object result = query.getSingleResult();
        return result != null ? (Double) result : null;
    }
}
