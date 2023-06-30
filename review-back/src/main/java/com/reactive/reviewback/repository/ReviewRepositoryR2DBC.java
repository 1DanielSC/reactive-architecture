package com.reactive.reviewback.repository;

import java.util.Optional;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.reactive.reviewback.model.Review;
import com.reactive.reviewback.model.ReviewR2DBC;

@Repository
public interface ReviewRepositoryR2DBC extends R2dbcRepository<ReviewR2DBC, Long>{
    Optional<Review> findByProductName(String productName);
}
