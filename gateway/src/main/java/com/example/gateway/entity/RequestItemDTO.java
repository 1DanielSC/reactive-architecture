package com.example.gateway.entity;

import java.io.Serializable;

public class RequestItemDTO implements Serializable{
    private String id;
    private ItemDTO item;
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public ItemDTO getItem() {
        return item;
    }
    public void setItem(ItemDTO item) {
        this.item = item;
    }
}
