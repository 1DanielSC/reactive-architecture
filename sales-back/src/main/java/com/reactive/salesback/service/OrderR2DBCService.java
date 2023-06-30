package com.reactive.salesback.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.reactive.salesback.model.OrderR2DBC;
import com.reactive.salesback.repository.OrderRepositoryR2DBC;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrderR2DBCService {

    private final OrderRepositoryR2DBC orderRepository;

    @Autowired
    public OrderR2DBCService(OrderRepositoryR2DBC orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Mono<OrderR2DBC> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public Flux<OrderR2DBC> getAllOrders() {
        return orderRepository.findAll();
    }

    public Mono<OrderR2DBC> createOrder(OrderR2DBC order) {
        return orderRepository.save(order);
    }

    public Mono<OrderR2DBC> updateOrder(Long id, OrderR2DBC order) {
        return orderRepository.findById(id)
                .flatMap(existingOrder -> {
                    existingOrder.setTotalPrice(order.getTotalPrice());
                    existingOrder.setStatus(order.getStatus());
                    existingOrder.setDate(order.getDate());
                    existingOrder.setOrderItems(order.getOrderItems());
                    return orderRepository.save(existingOrder);
                });
    }

    public Mono<Void> deleteOrder(Long id) {
        return orderRepository.deleteById(id);
    }
}
