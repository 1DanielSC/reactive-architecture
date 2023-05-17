package com.reactive.productback.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reactive.productback.model.Product;
import com.reactive.productback.service.ProductService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("product")
public class ProductController {
    
    @Autowired
    private ProductService service;

    @GetMapping("/thread")
    public Mono<String> thread(){
        return Mono.just(Thread.currentThread().toString());
    }

    @GetMapping
    public Flux<Product> findAll(){
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Product> findById(@PathVariable String id){
        return service.findById(id);
    }

    @GetMapping("/name/{name}")
    public Mono<Product> findByName(@PathVariable String name){
        return service.findByName(name);
    }

    @PostMapping
    public Mono<Product> save(@RequestBody Product entity){
        return service.save(entity);//TODO: CHECK HERE .subscribeOn(Schedulers.boundedElastic());
    }

    @PutMapping("/request")
    public Mono<Product> requestProduct(@RequestBody(required = true) Product entity){
        return service.requestProduct(entity);
    }

    @PutMapping(value = "/products")
    public Flux<Product> increaseQuantity(@RequestBody List<Product> products){
        return service.increaseQuantity(products);
    }

    @DeleteMapping(value = "/{name}")
    public Mono<Void> deleteAllByName(@PathVariable String name){
        return service.deleteAllByName(name);
    }
}
