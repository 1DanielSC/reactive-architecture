package com.reactive.reviewback.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.reactive.reviewback.model.dtos.ProductDTO;
import com.reactive.reviewback.service.request.ProductServiceClient;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import reactor.core.publisher.Mono;

@Service
public class ProductServiceResilience {
    
    @Autowired
    private ProductServiceClient productClient;

    @CircuitBreaker(name = "productservice", fallbackMethod = "circuitFallBack")
    @RateLimiter(name = "rate_productservice", fallbackMethod = "rateFallBack")
    @Bulkhead(name = "bulk_productservice", fallbackMethod = "bulkheadFallBack")
    @Retry(name = "retry_productservice", fallbackMethod = "retryFallBack")
    public Mono<ProductDTO> findByName(String name){
        return productClient.findByName(name);
    }

    public Mono<ProductDTO> circuitFallBack(Throwable ex){
        System.out.println("Circuit Breaker (fallback): "+ex.getMessage());        
        return Mono.empty();
    }

    public Mono<ProductDTO> rateFallBack(Throwable ex){
        System.out.println("Rate Limiter (fallback): "+ex.getMessage());        
        return Mono.empty();
    }

    public Mono<ProductDTO> bulkheadFallBack(Throwable ex){
        System.out.println("Bulkhead (fallback): "+ex.getMessage());        
        return Mono.empty();
    }

    public Mono<ProductDTO> retryFallBack(Throwable ex){
        System.out.println("Retry (fallback): "+ex.getMessage());        
        return Mono.empty();
    }
}
