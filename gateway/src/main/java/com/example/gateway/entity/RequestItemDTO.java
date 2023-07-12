package com.example.gateway.entity;

import java.io.Serializable;

public class RequestItemDTO implements Serializable{
    private String id;
    private Item item;
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public Item getItem() {
        return item;
    }
    public void setItem(Item item) {
        this.item = item;
    }
}
