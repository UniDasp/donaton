package com.donaton.logistics.repository;

import com.donaton.logistics.model.LogisticsEnvio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnvioRepository extends JpaRepository<LogisticsEnvio, Long> {

    List<LogisticsEnvio> findByDonacionId(Long donacionId);
}