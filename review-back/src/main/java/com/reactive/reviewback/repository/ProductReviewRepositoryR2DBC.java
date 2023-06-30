package com.reactive.reviewback.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.reactive.reviewback.model.ProductReview;
import com.reactive.reviewback.model.ProductReviewR2DBC;

import reactor.core.publisher.Mono;

@Repository
public interface ProductReviewRepositoryR2DBC extends R2dbcRepository<ProductReviewR2DBC, Long>{
    Mono<ProductReview> findByName(String name);
}
