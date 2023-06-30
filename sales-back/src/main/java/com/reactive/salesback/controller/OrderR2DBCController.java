package com.reactive.salesback.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reactive.salesback.model.OrderR2DBC;
import com.reactive.salesback.service.OrderR2DBCService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/orders")
public class OrderR2DBCController {

    private final OrderR2DBCService orderService;

    @Autowired
    public OrderR2DBCController(OrderR2DBCService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{id}")
    public Mono<OrderR2DBC> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @GetMapping
    public Flux<OrderR2DBC> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PostMapping
    public Mono<OrderR2DBC> createOrder(@RequestBody OrderR2DBC order) {
        return orderService.createOrder(order);
    }

    @PutMapping("/{id}")
    public Mono<OrderR2DBC> updateOrder(@PathVariable Long id, @RequestBody OrderR2DBC order) {
        return orderService.updateOrder(id, order);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteOrder(@PathVariable Long id) {
        return orderService.deleteOrder(id);
    }
}
