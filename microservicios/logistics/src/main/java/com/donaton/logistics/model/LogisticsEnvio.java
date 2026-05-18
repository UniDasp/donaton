package com.donaton.logistics.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogisticsEnvio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String direccion;

    private String estado;
    // Ej: PENDIENTE, EN_TRANSITO, ENTREGADO

    private Long donacionId;
    // referencia al microservicio de donaciones
}