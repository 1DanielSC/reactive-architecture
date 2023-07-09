package com.reactive.salesback.model.dtos;

import com.reactive.salesback.model.Item;

public class RequestItemDTO {
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
