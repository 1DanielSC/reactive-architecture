package com.reactive.reviewback.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.reactive.reviewback.model.ReviewR2DBC;
import com.reactive.reviewback.repository.ReviewRepositoryR2DBC;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ReviewR2DBCService {

    private final ReviewRepositoryR2DBC reviewRepository;

    @Autowired
    public ReviewR2DBCService(ReviewRepositoryR2DBC reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public Mono<ReviewR2DBC> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    public Flux<ReviewR2DBC> getAllReviews() {
        return reviewRepository.findAll();
    }

    public Mono<ReviewR2DBC> createReview(ReviewR2DBC review) {
        return reviewRepository.save(review);
    }

    public Mono<ReviewR2DBC> updateReview(Long id, ReviewR2DBC review) {
        return reviewRepository.findById(id)
                .flatMap(existingReview -> {
                    existingReview.setProductName(review.getProductName());
                    existingReview.setReview(review.getReview());
                    existingReview.setRating(review.getRating());
                    existingReview.setProductReview(review.getProductReview());
                    return reviewRepository.save(existingReview);
                });
    }

    public Mono<Void> deleteReview(Long id) {
        return reviewRepository.deleteById(id);
    }
}
