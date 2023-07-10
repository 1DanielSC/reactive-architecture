package com.reactive.salesback.model.dtos;

import java.io.Serializable;

public class ProductDTO implements Serializable {
    public String name;
    public Long quantity;
    public Double price;

    public ProductDTO(){
        
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Long getQuantity() {
        return quantity;
    }
    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }
    public Double getPrice() {
        return price;
    }
    public void setPrice(Double price) {
        this.price = price;
    }

    
}
