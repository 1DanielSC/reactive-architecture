package com.reactive.productback.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.reactive.productback.model.Product;

import reactor.core.publisher.Mono;


@Repository
public interface ProductRepository extends ReactiveMongoRepository<Product, String>{
 
    Mono<Product> findByName(String name);

    Mono<Void> deleteAllByName(String name);
}
