package com.reactive.reviewback.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.reactive.reviewback.model.Review;

@Repository
public interface ReviewRepository extends ReactiveMongoRepository<Review, String>{
    Optional<Review> findByProductName(String productName);
}
