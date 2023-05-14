package com.reactive.salesback.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.reactive.salesback.model.Order;

@Repository
public interface OrderRepository extends ReactiveMongoRepository<Order, String>{
    
}
