package com.reactive.salesback.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.reactive.salesback.model.enums.EnumStatusOrder;

@Document(collection = "orders")
public class Order {
    
    @Id
    private String id;
    private List<Item> items;
    private Double totalPrice;
    private EnumStatusOrder status;
    private LocalDateTime date;

    public Order(){
        this.totalPrice = 0.0;
        this.status = EnumStatusOrder.CREATED;
        this.items = new ArrayList<>();
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public List<Item> getItems() {
        return items;
    }
    public void setItems(List<Item> items) {
        this.items = items;
    }
    public EnumStatusOrder getStatus() {
        return status;
    }
    public void setStatus(EnumStatusOrder status) {
        this.status = status;
    }
    public LocalDateTime getDate() {
        return date;
    }
    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    
}
