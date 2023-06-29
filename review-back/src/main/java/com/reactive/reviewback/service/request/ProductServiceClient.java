package com.reactive.reviewback.service.request;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import com.reactive.reviewback.model.dtos.ProductDTO;

import reactor.core.publisher.Mono;

@HttpExchange
public interface ProductServiceClient {
    
    @GetExchange("/reactive-product/name/{name}")
    Mono<ProductDTO> findByName(@PathVariable String name);
}
