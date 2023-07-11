package com.reactive.productback.service;

import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.MimeTypeUtils;

import com.reactive.productback.exception.NotFoundException;
import com.reactive.productback.model.Product;
import com.reactive.productback.model.dtos.OrderConfirmationDTO;
import com.reactive.productback.repository.ProductRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Configuration
public class ProductService {
    
    @Autowired
    private ProductRepository repository;

    @Autowired
    private StreamBridge bridge;

    @Bean
    public Supplier<Flux<Product>> findAll(){
        return () -> repository.findAll();
    }

    @Bean
    public Function<Mono<Product>, Mono<Product>> save(){
        return productMono -> {
            return 
            productMono.flatMap(product -> {

                return repository.findByName(product.getName())
                .switchIfEmpty(
                    Mono.just(new Product(null, product.getName(), 0L, product.getPrice()))
                )
                .flatMap(e -> {
                    e.setQuantity(e.getQuantity()+product.getQuantity());
                    return Mono.just(e);
                })
                .flatMap(repository::save);
            });

        };
    }

    @Bean
    public Function<Mono<String>, Mono<Product>> findByName(){
        return name -> {
            return name.flatMap(e -> {
                return repository.findByName(e);
            });
        };
    }

    @Bean
    public Function<Mono<String>, Mono<Product>> findById(){
        return id -> {
            return id.flatMap(e -> {
                System.out.println("ID: " + e);
                return repository.findById(e);
            });
        }; 
    }

    public Mono<Product> update(Product entity){        
        return repository.findById(entity.getId())
        .switchIfEmpty(Mono.error(new NotFoundException("Product not found.")))
        .flatMap(e -> repository.save(entity));
    }

    @Bean
    public Function<Mono<OrderConfirmationDTO>, Mono<Product>> requestProduct(){
        return productMono -> {

            return productMono.flatMap(
            orderConfirmation -> {
                OrderConfirmationDTO confirmation = new OrderConfirmationDTO();
                confirmation.setProduct(orderConfirmation.getProduct());

                System.out.println("Product-Service: requestProduct");
                System.out.println("Nome: " + orderConfirmation.getProduct().getName());
                System.out.println("Qtd: " + orderConfirmation.getProduct().getQuantity());
                 
                return repository.findByName(orderConfirmation.getProduct().getName())
                .switchIfEmpty(
                    Mono.error(new NotFoundException("The product \"" + orderConfirmation.getProduct().getName() +"\" was not found."))
                )
                .flatMap(entity -> {
                    System.out.println("estou aqui 1...");
                    if(entity.getQuantity() >= orderConfirmation.getProduct().getQuantity()){
                        long quantityLeft = entity.getQuantity() - orderConfirmation.getProduct().getQuantity();
                        entity.setQuantity(quantityLeft);

                        System.out.println("ID order: " + orderConfirmation.getIdOrder());
                        confirmation.setIdOrder(orderConfirmation.getIdOrder());
                        confirmation.setProductOK(true);

                        System.out.println("Produto está disponível!");
                        return update(entity)
                        .flatMap(updated -> {
                            System.out.println("Enviando confirmação...");
                            bridge.send("confirm-order-input", confirmation, MimeTypeUtils.APPLICATION_JSON);
                            orderConfirmation.getProduct().setPrice(updated.getPrice());
                            return Mono.just(orderConfirmation.getProduct());
                        });
                    }
                    else{
                        confirmation.setReason("Não há quantidade suficiente deste produto.");
                        confirmation.setProductOK(false);
                    }
                    
                    System.out.println("Vou enviar...");
                    bridge.send("confirm-order-input", confirmation, MimeTypeUtils.APPLICATION_JSON);

                    return Mono.empty();
                });
            });      
        };
    }

    
    @Bean
    public Function<Flux<Product>, Flux<Product>> increaseQuantity(){
        return productsFlux -> {

            return
            productsFlux.flatMap(products -> {
                Mono<Boolean> hasElements = productsFlux.hasElements();
                Flux<Product> a = hasElements.flatMapMany(has -> has ? productsFlux : Flux.empty());
                a = a.switchIfEmpty(Flux.empty());
                
                return a.flatMap(e -> {
                    Mono<Product> productMono = repository.findByName(e.getName());
                    return productMono.switchIfEmpty(
                            Mono.error(new NotFoundException("Product with name \"" + e.getName() + "\" not found."))
                        )
                        .flatMap(product -> {
                            product.setQuantity(e.getQuantity() + product.getQuantity());
                            return update(product);
                        });
                });
            });
            
            
        };
    }
    
    // @Bean
    // public Function<List<Product>, Flux<Product>> increaseQuantity(){
    //     return products -> {
    //         if(products.size() == 0)
    //             return Flux.empty();

    //         return Flux.fromIterable(products)
    //         .flatMap(e -> {
    //             Mono<Product> productMono = repository.findByName(e.getName());
    //             return productMono.switchIfEmpty(
    //                 Mono.error(new NotFoundException("Product with name \"" + e.getName() +"\" not found."))
    //             )
    //             .flatMap(product -> {
    //                 product.setQuantity(e.getQuantity()+product.getQuantity());
    //                 return update(product);
    //             });
    //         });
    //     };
    // }
    
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
