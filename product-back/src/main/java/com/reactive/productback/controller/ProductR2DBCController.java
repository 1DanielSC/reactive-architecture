package com.reactive.productback.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reactive.productback.model.ProductR2DBC;
import com.reactive.productback.service.ProductR2DBCService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/products")
public class ProductR2DBCController {
    private final ProductR2DBCService productService;

    @Autowired
    public ProductR2DBCController(ProductR2DBCService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Flux<ProductR2DBC> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public Mono<ProductR2DBC> getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PostMapping
    public Mono<ProductR2DBC> createProduct(@RequestBody ProductR2DBC product) {
        return productService.createProduct(product);
    }

    @PutMapping("/{id}")
    public Mono<ProductR2DBC> updateProduct(@PathVariable Long id, @RequestBody ProductR2DBC product) {
        return productService.updateProduct(id, product);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteProduct(@PathVariable Long id) {
        return productService.deleteProduct(id);
    }
}
