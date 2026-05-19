package com.donaton.logistics.repository;

import com.donaton.logistics.model.LogisticsEnvio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EnvioRepository extends JpaRepository<LogisticsEnvio, Long> {

    Optional<LogisticsEnvio> findByDonacionId(Long donacionId);

    List<LogisticsEnvio> findByAcopioCenterId(String acopioCenterId);

    List<LogisticsEnvio> findByEstadoAndAcopioDeadlineBefore(String estado, Instant deadline);
}
