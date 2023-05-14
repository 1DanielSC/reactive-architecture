package com.reactive.salesback.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "items")
public class Item {
    
    @Id
    private String id;
    private String name;
    private Long quantity;
    private Double price;

    public Item(){
        this.quantity = (long) 0;
        this.price=0.0;
    }

    public Item(String name, Double price){
        this.name=name;
        this.price=price;
        this.quantity=(long)0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    
}
