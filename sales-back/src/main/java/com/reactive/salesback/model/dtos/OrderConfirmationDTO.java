package com.reactive.salesback.model.dtos;

import java.io.Serializable;

public class OrderConfirmationDTO implements Serializable {

    private String idOrder;
    private ProductDTO product;
    private boolean productOK;
    private String reason;

    public String getIdOrder() {
        return idOrder;
    }
    public void setIdOrder(String idOrder) {
        this.idOrder = idOrder;
    }
    public ProductDTO getProduct() {
        return product;
    }
    public void setProduct(ProductDTO product) {
        this.product = product;
    }
    public boolean isProductOK() {
        return productOK;
    }
    public void setProductOK(boolean productOK) {
        this.productOK = productOK;
    }
    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }
}
