package com.reactive.productback.model.dtos;

import java.io.Serializable;

import com.reactive.productback.model.Product;

public class OrderConfirmationDTO implements Serializable {

    private String idOrder;
    private Product product;
    private boolean productOK;
    private String reason;

    public String getIdOrder() {
        return idOrder;
    }
    public void setIdOrder(String idOrder) {
        this.idOrder = idOrder;
    }
    public Product getProduct() {
        return product;
    }
    public void setProduct(Product product) {
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
