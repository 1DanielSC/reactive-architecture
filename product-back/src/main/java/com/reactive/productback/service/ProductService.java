package com.reactive.productback.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.reactive.productback.exception.NotFoundException;
import com.reactive.productback.model.Product;
import com.reactive.productback.repository.ProductRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository repository;

    public Flux<Product> findAll(){
        return repository.findAll();
    }

    public Mono<Product> findById(String id){
        return repository.findById(id);
    }

    public Mono<Void> deleteAllByName(String name){
        return repository.deleteAllByName(name);
    }

    public Mono<Product> save(Product entity){
        return findByName(entity.getName())
        .switchIfEmpty(Mono.just(new Product(null, entity.getName(), 0L, entity.getPrice())))
        .doOnNext(e -> {
            System.out.println(Thread.currentThread().toString());
        })
        .flatMap(e -> {
            System.out.println(Thread.currentThread().toString());
            e.setQuantity(e.getQuantity()+entity.getQuantity());
            return Mono.just(e);
        })
        .doOnNext(e -> {
            System.out.println(Thread.currentThread().toString());
        })
        .flatMap(repository::save)
        .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Product> findByName(String name){
        //return repository.findByName(name).publishOn(Schedulers.fromExecutorService(Executors.newVirtualThreadPerTaskExecutor()))
        return repository.findByName(name)
        .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Product> update(Product entity){        
        Mono<Product> product = repository.findById(entity.getId());
        return product.switchIfEmpty(Mono.error(new NotFoundException("Product not found.")))
        .flatMap( e -> {
            return repository.save(entity);
            /*
             * O Problema aqui era que eu estava passando o parâmetro "e" no comando acima (return repository.save(entity))
             * 
             * O parametro "e" é apenas o resultado da busca por Id feita pelo repository.
             */
        });
         
    }

    public Mono<Product> requestProduct(Product entity){
        Mono<Product> product = repository.findByName(entity.getName());
        
        return product.switchIfEmpty(Mono.error(new NotFoundException("Product not found.")))
        .flatMap(e -> {
            if(e.getQuantity() >= entity.getQuantity()){
                long quantityLeft = e.getQuantity() - entity.getQuantity();
                e.setQuantity(quantityLeft);
                return update(e).flatMap(updated -> {
                    entity.setPrice(updated.getPrice());
                    return Mono.just(entity);
                });
            }
            return Mono.empty();
        });
    }

    public Flux<Product> increaseQuantity(List<Product> products){
        if(products.size() == 0)
            return Flux.empty();

        return Flux.fromIterable(products)
        .flatMap(e -> {
            Mono<Product> productMono = repository.findByName(e.getName());
            return productMono.switchIfEmpty(Mono.error(new NotFoundException("Product with name \"" + e.getName() +"\" not found.")))
            .flatMap(product -> {
                product.setQuantity(e.getQuantity()+product.getQuantity());
                return repository.save(product);
            });
        });
    }
}
