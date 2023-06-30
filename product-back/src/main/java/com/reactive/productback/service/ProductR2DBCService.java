package com.reactive.productback.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.reactive.productback.model.ProductR2DBC;
import com.reactive.productback.repository.ProductRepositoryR2DBC;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductR2DBCService {
    private final ProductRepositoryR2DBC productRepository;

    @Autowired
    public ProductR2DBCService(ProductRepositoryR2DBC productRepository) {
        this.productRepository = productRepository;
    }

    public Flux<ProductR2DBC> getAllProducts() {
        return productRepository.findAll();
    }

    public Mono<ProductR2DBC> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Mono<ProductR2DBC> createProduct(ProductR2DBC product) {
        return productRepository.save(product);
    }

    public Mono<ProductR2DBC> updateProduct(Long id, ProductR2DBC updatedProduct) {
        return productRepository.findById(id)
                .flatMap(existingProduct -> {
                    existingProduct.setName(updatedProduct.getName());
                    existingProduct.setQuantity(updatedProduct.getQuantity());
                    existingProduct.setPrice(updatedProduct.getPrice());
                    return productRepository.save(existingProduct);
                });
    }

    public Mono<Void> deleteProduct(Long id) {
        return productRepository.deleteById(id);
    }
}
