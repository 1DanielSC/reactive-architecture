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

import com.reactive.reviewback.model.ReviewR2DBC;
import com.reactive.reviewback.service.ReviewR2DBCService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/reviews")
public class ReviewR2DBCController {

    private final ReviewR2DBCService reviewService;

    @Autowired
    public ReviewR2DBCController(ReviewR2DBCService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/{id}")
    public Mono<ReviewR2DBC> getReviewById(@PathVariable Long id) {
        return reviewService.getReviewById(id);
    }

    @GetMapping
    public Flux<ReviewR2DBC> getAllReviews() {
        return reviewService.getAllReviews();
    }

    @PostMapping
    public Mono<ReviewR2DBC> createReview(@RequestBody ReviewR2DBC review) {
        return reviewService.createReview(review);
    }

    @PutMapping("/{id}")
    public Mono<ReviewR2DBC> updateReview(@PathVariable Long id, @RequestBody ReviewR2DBC review) {
        return reviewService.updateReview(id, review);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteReview(@PathVariable Long id) {
        return reviewService.deleteReview(id);
    }
}