package com.reactive.reviewback.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.reactive.reviewback.model.Review;
import com.reactive.reviewback.repository.ReviewRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository repository;

    public Mono<Review> findById(String id){
        return repository.findById(id);
    }

    public Flux<Review> findAll(){
        return repository.findAll();
    }
}
