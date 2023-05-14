package com.reactive.reviewback.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.reactive.reviewback.model.ProductReview;

import reactor.core.publisher.Mono;

@Repository
public interface ProductReviewRepository extends ReactiveMongoRepository<ProductReview, String> {
    
    Mono<ProductReview> findByProductName(String productName);
}
