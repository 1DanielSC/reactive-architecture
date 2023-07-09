package com.reactive.productback.service;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.reactive.productback.exception.NotFoundException;
import com.reactive.productback.model.Product;
import com.reactive.productback.repository.ProductRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Configuration
public class ProductService {
    
    @Autowired
    private ProductRepository repository;

    @Bean
    public Supplier<Flux<Product>> findAll(){
        return () -> repository.findAll();
    }

    @Bean
    public Function<Product, Mono<Product>> save(){
        return product -> {
            return repository.findByName(product.getName())
            .switchIfEmpty(
                Mono.just(new Product(null, product.getName(), 0L, product.getPrice()))
            )
            .flatMap(e -> {
                e.setQuantity(e.getQuantity()+product.getQuantity());
                return Mono.just(e);
            })
            .flatMap(repository::save);
        };
    }

    @Bean
    public Function<String, Mono<Product>> findByName(){
        return name -> repository.findByName(name);
    }

    @Bean
    public Function<String, Mono<Product>> findById(){
        return id -> repository.findById(id);
    }

    public Mono<Product> update(Product entity){        
        return repository.findById(entity.getId())
        .switchIfEmpty(Mono.error(new NotFoundException("Product not found.")))
        .flatMap(e -> repository.save(entity));
    }

    @Bean
    public Function<Product, Mono<Product>> requestProduct(){
        return productReceived -> {

            return 
            repository.findByName(productReceived.getName())
            .switchIfEmpty(
                Mono.error(new NotFoundException("Product with the informed name was not found."))
            )
            .flatMap(entity -> {
                if(entity.getQuantity() >= productReceived.getQuantity()){
                long quantityLeft = entity.getQuantity() - productReceived.getQuantity();
                entity.setQuantity(quantityLeft);
                return update(entity)
                .flatMap(updated -> {
                    productReceived.setPrice(updated.getPrice());
                    return Mono.just(productReceived);
                });
            }
            return Mono.empty();
            });
        };
    }

    @Bean
    public Function<List<Product>, Flux<Product>> increaseQuantity(){
        return products -> {
            if(products.size() == 0)
                return Flux.empty();

            return Flux.fromIterable(products)
            .flatMap(e -> {
                Mono<Product> productMono = repository.findByName(e.getName());
                return productMono.switchIfEmpty(
                    Mono.error(new NotFoundException("Product with name \"" + e.getName() +"\" not found."))
                )
                .flatMap(product -> {
                    product.setQuantity(e.getQuantity()+product.getQuantity());
                    return update(product);
                });
            });
        };
    }


    // public Flux<Product> findAll(){
    //     return repository.findAll();
    // }

    // public Mono<Product> findById(String id){
    //     return productCache.get("product:"+id)
    //         .switchIfEmpty(
    //             repository.findById(id)
    //             .doOnNext(e -> System.out.println("FindById: vou salvar no cache..."))
    //             .flatMap(c -> productCache.fastPut("product:"+c.getId(), c)
    //                                         .thenReturn(c))
    //         );

    //     // return repository.findById(id)
    //     // .switchIfEmpty(Mono.error(new NotFoundException("Product not found with this id.")));
    // }

    // public Mono<Void> deleteAllByName(String name){
    //     productCache.fastRemove("product:"+name).thenReturn(null).subscribe();
    //     return repository.deleteAllByName(name);
    // }

    // public Mono<Product> save(Product entity){
    //     return findByName(entity.getName())
    //     .switchIfEmpty(Mono.just(new Product(null, entity.getName(), 0L, entity.getPrice())))
    //     .flatMap(e -> {
    //         e.setQuantity(e.getQuantity()+entity.getQuantity());
    //         return Mono.just(e);
    //     })
    //     .flatMap(repository::save)
    //     .flatMap(e -> updateOrSaveOnCache(e));
    // }

    // public Mono<Product> findByName(String name){
    //     return Mono.defer(() -> findByNameCached(name))
    //     .subscribeOn(Schedulers.boundedElastic())
    //     .publishOn(Schedulers.boundedElastic());
    //     //.publishOn(Schedulers.fromExecutorService(executorService));
    // }

    // public Mono<Product> findByNameCached(String name){
    //     return productCache.get("product:"+name)
    //     .switchIfEmpty(repository.findByName(name).doOnNext(e -> System.out.println("Vou buscar no banco"))
    //     .flatMap(e -> productCache.fastPut("product:"+name, e).thenReturn(e)));
    // }

    // /**
    //  * Atualiza cache com nova entidade de Product.
    //  * Adiciona cria/atualiza dois registros: id e nome
    //  */
    // private Mono<Product> updateOrSaveOnCache(Product entity){
    //     return productCache.fastPut("product:"+entity.getName(), entity)                  
    //     .thenReturn(entity)          
    //     .flatMap(e -> productCache.fastPut("product:"+e.getId(), e).thenReturn(e))              
    //     .subscribeOn(Schedulers.boundedElastic());
    // }

    // public Mono<Product> update(Product entity){        
    //     return repository.findById(entity.getId())
    //     .switchIfEmpty(Mono.error(new NotFoundException("Product not found.")))
    //     .flatMap(e -> repository.save(entity))
    //     .flatMap(e -> updateOrSaveOnCache(e));
    // }

    // public Mono<Product> requestProduct(Product entity){
    //     Mono<Product> product = repository.findByName(entity.getName());
        
    //     return product.switchIfEmpty(Mono.error(new NotFoundException("Product not found.")))
    //     .flatMap(e -> {
    //         if(e.getQuantity() >= entity.getQuantity()){
    //             long quantityLeft = e.getQuantity() - entity.getQuantity();
    //             e.setQuantity(quantityLeft);
    //             return update(e).flatMap(updated -> {
    //                 entity.setPrice(updated.getPrice());
    //                 return Mono.just(entity);
    //             });
    //         }
    //         return Mono.empty();
    //     });
    // }

    // public Flux<Product> increaseQuantity(List<Product> products){
    //     if(products.size() == 0)
    //         return Flux.empty();

    //     return Flux.fromIterable(products)
    //     .flatMap(e -> {
    //         Mono<Product> productMono = repository.findByName(e.getName());
    //         return productMono.switchIfEmpty(Mono.error(new NotFoundException("Product with name \"" + e.getName() +"\" not found.")))
    //         .flatMap(product -> {
    //             product.setQuantity(e.getQuantity()+product.getQuantity());
    //             return update(product);
    //         });
    //     });
    // }

    // public Mono<Void> deleteAll(){
    //     productCache.delete().subscribe();
    //     return repository.deleteAll();
    // }
}
