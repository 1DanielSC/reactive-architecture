package com.reactive.reviewback.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reactive.reviewback.model.ProductReview;
import com.reactive.reviewback.model.Review;
import com.reactive.reviewback.service.ProductReviewService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("product-review")
public class ProductReviewController {
    
    @Autowired
    private ProductReviewService service;

    @GetMapping
    public Flux<ProductReview> findAll(){
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Mono<ProductReview> findById(@PathVariable String id){
        return service.findById(id);
    }

    @PostMapping
    public Mono<ProductReview> save(@RequestBody Review review){
        return service.save(review);
    }

}
