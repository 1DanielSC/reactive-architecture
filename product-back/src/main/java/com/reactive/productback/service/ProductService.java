package com.reactive.productback.service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.redisson.api.RMapCacheReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.codec.TypedJsonJacksonCodec;
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

    private RMapCacheReactive<String, Product> productCache;

    public ProductService(RedissonReactiveClient client) {
        this.productCache = client.getMapCache("/product/", new TypedJsonJacksonCodec(String.class, Product.class));
    }

    //private ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    public Flux<Product> findAll(){
        return repository.findAll();
    }

    public Mono<Product> findById(String id){
        return productCache.get("product:"+id)
            .switchIfEmpty(
                repository.findById(id)
                .doOnNext(e -> System.out.println("FindById: vou salvar no cache..."))
                .flatMap(c -> productCache.fastPut("product:"+c.getId(), c)
                                            .thenReturn(c))
            );

        //return repository.findById(id);
    }

    public Mono<Void> deleteAllByName(String name){
        productCache.delete().subscribe();
        return repository.deleteAllByName(name);
    }

    public Mono<Product> save(Product entity){
        return findByName(entity.getName())
        .switchIfEmpty(Mono.just(new Product(null, entity.getName(), 0L, entity.getPrice())))
        .flatMap(e -> {
            e.setQuantity(e.getQuantity()+entity.getQuantity());
            return Mono.just(e);
        })
        .flatMap(repository::save)
        .flatMap(e -> updateOrSaveOnCache(e));
    }

    public Mono<Product> findByName(String name){
        return Mono.defer(() -> findByNameCached(name))
        .subscribeOn(Schedulers.boundedElastic())
        .publishOn(Schedulers.boundedElastic());
        //.publishOn(Schedulers.fromExecutorService(executorService));
    }

    public Mono<Product> findByNameCached(String name){
        return productCache.get("product:"+name)
        .switchIfEmpty(repository.findByName(name).doOnNext(e -> System.out.println("Vou buscar no banco"))
        .flatMap(e -> productCache.fastPut("product:"+name, e).thenReturn(e)));
    }

    /**
     * Atualiza cache com nova entidade de Product.
     * Adiciona cria/atualiza dois registros: id e nome
     */
    private Mono<Product> updateOrSaveOnCache(Product entity){
        return productCache.fastPut("product:"+entity.getName(), entity)                  
        .thenReturn(entity)          
        .flatMap(e -> productCache.fastPut("product:"+e.getId(), e).thenReturn(e))              
        .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Product> update(Product entity){        
        return repository.findById(entity.getId())
        .switchIfEmpty(Mono.error(new NotFoundException("Product not found.")))
        .flatMap(e -> repository.save(entity))
        .flatMap(e -> updateOrSaveOnCache(e));
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

    public Mono<Void> deleteAll(){
        return repository.deleteAll();
    }
}
