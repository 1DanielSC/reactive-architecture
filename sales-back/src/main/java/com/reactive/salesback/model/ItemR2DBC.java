package com.reactive.salesback.model;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("items")
public class ItemR2DBC implements Serializable{
    @Id
    @Column("id")
    private Long id; // Alterado para o tipo Long para suportar a estratégia SERIAL

    @Column("name")
    private String name;

    @Column("quantity")
    private Long quantity;

    @Column("price")
    private Double price;

    public ItemR2DBC() {
        this.quantity = 0L;
        this.price = 0.0;
    }

    public ItemR2DBC(String name, Double price) {
        this.name = name;
        this.price = price;
        this.quantity = 0L;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
