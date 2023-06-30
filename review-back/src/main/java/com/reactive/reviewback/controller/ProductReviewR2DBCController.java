package com.reactive.reviewback.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reactive.reviewback.model.ProductReviewR2DBC;
import com.reactive.reviewback.service.ProductReviewR2DBCService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/product-reviews")
public class ProductReviewR2DBCController {

    private final ProductReviewR2DBCService productReviewService;

    @Autowired
    public ProductReviewR2DBCController(ProductReviewR2DBCService productReviewService) {
        this.productReviewService = productReviewService;
    }

    @GetMapping("/{id}")
    public Mono<ProductReviewR2DBC> getProductReviewById(@PathVariable Long id) {
        return productReviewService.getProductReviewById(id);
    }

    @GetMapping
    public Flux<ProductReviewR2DBC> getAllProductReviews() {
        return productReviewService.getAllProductReviews();
    }

    @PostMapping
    public Mono<ProductReviewR2DBC> createProductReview(@RequestBody ProductReviewR2DBC productReview) {
        return productReviewService.createProductReview(productReview);
    }

    @PutMapping("/{id}")
    public Mono<ProductReviewR2DBC> updateProductReview(@PathVariable Long id, @RequestBody ProductReviewR2DBC productReview) {
        return productReviewService.updateProductReview(id, productReview);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteProductReview(@PathVariable Long id) {
        return productReviewService.deleteProductReview(id);
    }
}
