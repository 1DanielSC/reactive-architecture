package com.reactive.salesback.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.reactive.salesback.model.enums.EnumStatusOrder;


@Table("orders")
public class OrderR2DBC implements Serializable{
    @Id
    @Column("id")
    private Long id; // Alterado para o tipo Long para suportar a estratégia SERIAL

    @Column("total_price")
    private Double totalPrice;

    @Column("status")
    private EnumStatusOrder status;

    @Column("date")
    private LocalDateTime date;

    // Mapeamento da relação entre Order e Item usando uma tabela intermediária
    @Column("order_item_id")
    private List<ItemR2DBC> orderItems;

    public OrderR2DBC() {
        this.totalPrice = 0.0;
        this.status = EnumStatusOrder.CREATED;
        this.date = LocalDateTime.now();
        this.orderItems = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
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

    public List<ItemR2DBC> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<ItemR2DBC> orderItems) {
        this.orderItems = orderItems;
    }

    
}
