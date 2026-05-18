package com.donaton.logistics.dto;

public class EnvioRequestDTO {

    private Long donacionId;
    private String direccion;
    private String estado;

    public EnvioRequestDTO() {
    }

    public Long getDonacionId() {
        return donacionId;
    }

    public void setDonacionId(Long donacionId) {
        this.donacionId = donacionId;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}