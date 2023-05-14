package com.reactive.reviewback.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reactive.reviewback.model.Review;
import com.reactive.reviewback.service.ReviewService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("review")
public class ReviewController {
    
    @Autowired
    private ReviewService service;

    @GetMapping
    public Flux<Review> findAll(){
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Review> findById(@PathVariable String id){
        return service.findById(id);
    }
}
