package com.reactive.salesback.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reactive.salesback.model.Item;
import com.reactive.salesback.model.Order;
import com.reactive.salesback.model.enums.EnumStatusOrder;
import com.reactive.salesback.service.OrderService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("order")
public class OrderController {
    
    @Autowired
    private OrderService service;

    @GetMapping
    public Flux<Order> findAll(){
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Order> findById(@PathVariable String id){
        return service.findById(id);
    }

    @PostMapping
    public Mono<Order> createOrder(){
        return service.createOrder();
    }

    @PatchMapping("/{id}/status/{status}")
    public Mono<Order> updateStatus(@PathVariable(value = "id") String orderId, @PathVariable(value = "status") EnumStatusOrder status){
        return service.updateStatus(orderId, status);
    }

    @PostMapping("/{id}/item")
    public Mono<Order> addItemToOrder(@PathVariable(value = "id") String orderId, @RequestBody(required = true) Item item){
        return service.addItemToOrder(orderId, item);
    }
}
