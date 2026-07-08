package com.donaton.donation.dto;

import com.donaton.donation.model.DonationModel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class DonationResponseDTO {

    private Long id;
    private String descripcion;
    private Double cantidad;
    private String tipo;
    private String direccion;
    private String needId;
    private String donorEmail;
    private String unit;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getDonorEmail() { return donorEmail; }
    public void setDonorEmail(String donorEmail) { this.donorEmail = donorEmail; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public static DonationResponseDTO fromModel(DonationModel model) {
        if (model == null) {
            return null;
        }
        DonationResponseDTO dto = new DonationResponseDTO();
        dto.setId(model.getId());
        dto.setDescripcion(model.getDescripcion());
        dto.setCantidad(model.getCantidad());
        dto.setTipo(model.getTipo());
        dto.setDireccion(model.getDireccion());
        dto.setNeedId(model.getNeedId());
        dto.setDonorEmail(model.getDonorEmail());
        dto.setUnit("unidad");
        return dto;
    }
}
