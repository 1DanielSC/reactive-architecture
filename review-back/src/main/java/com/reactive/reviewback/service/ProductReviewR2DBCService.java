package com.reactive.reviewback.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.reactive.reviewback.model.ProductReviewR2DBC;
import com.reactive.reviewback.repository.ProductReviewRepositoryR2DBC;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductReviewR2DBCService {

    private final ProductReviewRepositoryR2DBC productReviewRepository;

    @Autowired
    public ProductReviewR2DBCService(ProductReviewRepositoryR2DBC productReviewRepository) {
        this.productReviewRepository = productReviewRepository;
    }

    public Mono<ProductReviewR2DBC> getProductReviewById(Long id) {
        return productReviewRepository.findById(id);
    }

    public Flux<ProductReviewR2DBC> getAllProductReviews() {
        return productReviewRepository.findAll();
    }

    public Mono<ProductReviewR2DBC> createProductReview(ProductReviewR2DBC productReview) {
        return productReviewRepository.save(productReview);
    }

    public Mono<ProductReviewR2DBC> updateProductReview(Long id, ProductReviewR2DBC productReview) {
        return productReviewRepository.findById(id)
                .flatMap(existingProductReview -> {
                    existingProductReview.setName(productReview.getName());
                    existingProductReview.setReviews(productReview.getReviews());
                    existingProductReview.setRating(productReview.getRating());
                    return productReviewRepository.save(existingProductReview);
                });
    }

    public Mono<Void> deleteProductReview(Long id) {
        return productReviewRepository.deleteById(id);
    }
}
