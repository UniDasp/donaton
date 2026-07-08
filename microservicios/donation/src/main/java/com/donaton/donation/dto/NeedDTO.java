package com.donaton.donation.dto;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NeedDTO {

    private String id;
    private String category;
    private String status;
    private Double quantityRequired;
    private Double quantityReceived;
    private String centerId;
    private String centerName;
    private String address;
    private String createdByEmail;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getQuantityRequired() { return quantityRequired; }
    public void setQuantityRequired(Double quantityRequired) { this.quantityRequired = quantityRequired; }
    public Double getQuantityReceived() { return quantityReceived; }
    public void setQuantityReceived(Double quantityReceived) { this.quantityReceived = quantityReceived; }
    public String getCenterId() { return centerId; }
    public void setCenterId(String centerId) { this.centerId = centerId; }
    public String getCenterName() { return centerName; }
    public void setCenterName(String centerName) { this.centerName = centerName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCreatedByEmail() { return createdByEmail; }
    public void setCreatedByEmail(String createdByEmail) { this.createdByEmail = createdByEmail; }
}
