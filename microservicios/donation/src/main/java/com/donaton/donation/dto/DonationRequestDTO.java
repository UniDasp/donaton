package com.donaton.donation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DonationRequestDTO {

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    private String descripcion;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Double cantidad;

    @NotBlank(message = "El tipo es obligatorio")
    @Size(max = 100, message = "El tipo no puede superar 100 caracteres")
    private String tipo;

    @Size(max = 500, message = "La dirección no puede superar 500 caracteres")
    private String direccion;

    @Size(max = 64, message = "El identificador de necesidad no puede superar 64 caracteres")
    private String needId;

    @Size(max = 50, message = "La unidad no puede superar 50 caracteres")
    private String unit;

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Double getCantidad() { return cantidad; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getNeedId() { return needId; }
    public void setNeedId(String needId) { this.needId = needId; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
