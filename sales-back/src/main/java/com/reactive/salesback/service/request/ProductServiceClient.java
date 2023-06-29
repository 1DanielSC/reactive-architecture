package com.reactive.salesback.service.request;

import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PutExchange;

import com.reactive.salesback.model.Item;
import com.reactive.salesback.model.dtos.ProductDTO;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@HttpExchange
public interface ProductServiceClient {
    
    @PutExchange("/reactive-product/request")
    Mono<ProductDTO> requestProduct(@RequestBody(required = true) Item item);

    @PutExchange("/reactive-product/products")
    Flux<Item> increaseQuantity(@RequestBody List<Item> items);
}
