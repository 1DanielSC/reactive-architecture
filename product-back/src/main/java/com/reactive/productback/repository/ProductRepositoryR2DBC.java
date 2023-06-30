package com.reactive.productback.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.reactive.productback.model.Product;
import com.reactive.productback.model.ProductR2DBC;

import reactor.core.publisher.Mono;

@Repository
public interface ProductRepositoryR2DBC extends R2dbcRepository<ProductR2DBC, Long>{
    
    Mono<Product> findByName(String name);

    Mono<Void> deleteAllByName(String name);
}
