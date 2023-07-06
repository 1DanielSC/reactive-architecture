package com.reactive.salesback.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.reactive.salesback.exception.APIConnectionError;
import com.reactive.salesback.exception.GenericException;
import com.reactive.salesback.exception.InvalidDataException;
import com.reactive.salesback.exception.NotFoundException;
import com.reactive.salesback.exception.PreconditionFailedException;
import com.reactive.salesback.model.Item;
import com.reactive.salesback.model.Order;
import com.reactive.salesback.model.dtos.ProductDTO;
import com.reactive.salesback.model.enums.EnumStatusOrder;
import com.reactive.salesback.repository.OrderRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository repository;

    @Autowired
    private WebClient.Builder webClient;

    @Autowired
    private ProductServiceResilience productClient;

    public Flux<Order> findAll(){
        return repository.findAll();
    }

    public Mono<Order> findById(String id){
        return repository.findById(id);
    }
    
    public Mono<Order> createOrder(){
        Mono<Order> order = Mono.just(new Order());
        return order
        .flatMap(e -> {
            e.setId(null);
            e.setDate(LocalDateTime.now());
            e.setStatus(EnumStatusOrder.CREATED);
            return repository.save(e);
        });
    }

    public Mono<Order> updateStatus(String id, EnumStatusOrder status){
        Mono<Order> order = findById(id);
        return order
        .switchIfEmpty(Mono.error(new NotFoundException("Order not found with this id.")))
        .flatMap(e -> {
            if(status == EnumStatusOrder.APPROVED)
                return placeOrder(order);
            else if(status == EnumStatusOrder.CANCELED)
                return cancelOrder(order);
            else if(status == EnumStatusOrder.REFUSED)
                return refuseOrder(order);
            else
                return Mono.error(new IllegalArgumentException("Invalid status."));
        });
    }

    public Mono<Order> addItemToOrder(String orderId, Item item){

        return repository.findById(orderId)
        .switchIfEmpty(Mono.error(new NotFoundException("Order not found with this id.")))
        .flatMap(orderMono -> {            
            return requestProduct(item)
            .switchIfEmpty(Mono.error(new InvalidDataException("Nao foi possivel obter o product.")))
            .flatMap(product -> {
                item.setPrice(product.getPrice());                 
                Item itemFromStream = orderMono.getItems()
                .stream()
                .filter(productItem -> productItem.getName().equals(item.getName()))
                .findFirst()
                .orElse(new Item(item.getName(),item.getPrice()));


                if(itemFromStream.getQuantity() == 0)
                    orderMono.getItems().add(item); //TODO: Bad practice.
                itemFromStream.setQuantity(itemFromStream.getQuantity()+item.getQuantity());
                Double priceItem = item.getPrice()*item.getQuantity();
                orderMono.setTotalPrice(orderMono.getTotalPrice()+priceItem);
                return repository.save(orderMono); 
                
                /* 
                return Flux.fromIterable(orderMono.getItems())
                .filter(productItem -> productItem.getName().equals(item.getName()))
                // O Flux deferido dentro do switchIfEmpty retorna um Mono para que o fluxo possa continuar em flatMap. (switchIfEmpty + flatMap)
                .switchIfEmpty(Flux.defer(() -> {
                    System.out.println("Entrei no switch.");
                    Item t = new Item();
                    t.setName(item.getName());
                    t.setPrice(item.getPrice());
                    t.setQuantity(0);
                    orderMono.getItems().add(item);
                    return Flux.from(Mono.just(t));
                }))
                .flatMap(listItem -> {
                    System.out.println("Entrei no flatmap.");
                    listItem.setQuantity(listItem.getQuantity()+item.getQuantity());
                    Double priceItem = item.getPrice()*item.getQuantity();
                    orderMono.setTotalPrice(orderMono.getTotalPrice()+priceItem);
                    return repository.save(orderMono);   
                })
                .next();
                */
 
            });

        });
    }

    //TODO: Verificar como lidar com erros de requisição
    private Mono<ProductDTO> requestProduct(Item item){
        return Flux.range(0, 1)
        .parallel()
        .runOn(Schedulers.boundedElastic())
        .flatMap(e -> requestProductOnMicroservice(item))
        .sequential()
        .next();
    }

    private Mono<ProductDTO> requestProductOnMicroservice(Item item){
        return productClient.requestProduct(item);
    }

    


    private Mono<Order> placeOrder(Mono<Order> order){
        return order
        .switchIfEmpty(Mono.error(new NotFoundException("Order not found.")))
        .flatMap(e ->{
            e.setStatus(EnumStatusOrder.APPROVED);
            return repository.save(e);
        });
    }


    private Mono<Order> cancelOrder(Mono<Order> order){
        return order
        .switchIfEmpty(Mono.error(new NotFoundException("Order not found.")))
        .filter(e -> e.getStatus() == EnumStatusOrder.CREATED)
        .switchIfEmpty(Mono.error(new PreconditionFailedException("Order status is not CREATED.")))
        .doOnNext(e -> updateProductQuantity(e).subscribe())
        .flatMap(e ->{
            e.setStatus(EnumStatusOrder.CANCELED);
            return repository.save(e);
        });
    }
    private Mono<Order> refuseOrder(Mono<Order> order){
        return order
        .switchIfEmpty(Mono.error(new NotFoundException("Order not found.")))
        .filter(e -> e.getStatus() == EnumStatusOrder.CREATED)
        .switchIfEmpty(Mono.error(new PreconditionFailedException("Order status is not CREATED.")))
        .doOnNext(e -> updateProductQuantity(e).subscribe())
        .flatMap(e -> {
            e.setStatus(EnumStatusOrder.REFUSED);
            return repository.save(e);
        });
    }

    private Mono<Item> updateProductQuantity(Order order){
        return Flux.range(0, 1)
        .parallel()
        .runOn(Schedulers.boundedElastic())
        .flatMap(e -> updateOnProductMicroservice(order)) //TODO: Changing doOnNext to flatMap fixed the request problem
        .sequential()
        .next();
    }

    private Flux<Item> updateOnProductMicroservice(Order order){
        return productClient.increaseQuantity(order.getItems());
    }


    private Mono<ProductDTO> oldRequestProductOnMicroservice(Item item){
        return webClient.build()
        .put()
        .uri("/product/request")
        .body(Mono.just(item), Item.class) //Funcionou com o Mono.just(). Antes o product-back nem recebia a requisicao
        .retrieve()
        .onStatus(status -> status.value() == HttpStatus.SERVICE_UNAVAILABLE.value(),
             response -> Mono.error(new APIConnectionError("Connection to product-back has failed.")))
        .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(),
            response -> Mono.error(new NotFoundException("Product not found.")))
        .onStatus(status -> status.value() != HttpStatus.OK.value(),
            response -> Mono.error(new GenericException("Error on request to product-back server.")))
        .bodyToMono(ProductDTO.class);
    }

    private Flux<Item> oldUpdateOnProductMicroservice(Order order){
        return webClient.build()
        .put()
        .uri("/product/products")
        .bodyValue(order.getItems())
        .retrieve()
        .onStatus(status -> status.value() == HttpStatus.SERVICE_UNAVAILABLE.value(),
            response -> Mono.error(new APIConnectionError("Connection to product-back has failed.")))
        .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(),
            response -> Mono.error(new NotFoundException("Product not found.")))
        .onStatus(status -> status.value() != HttpStatus.OK.value(),
            response -> Mono.error(new GenericException("Error on request to product-back server.")))
        .bodyToFlux(Item.class);
    }

    

    /* NOTA SOBRE WEBCLIENT X RESTTEMPLATE */
            

        //Se eu tivesse utilizado RestTemplate, quando a linha abaixo for executada
            //O product já terá sua quantidade atualizada no servidor Product-Back
            //Isso acontece por causa da abordagem não-bloqueante (sincronismo)
            
            /*
             * Se algum dado fosse retornado dessa requisição, o Netty seria avisado que eu fiz meu trabalho
             *      e que algum dado estaria presente dentro do Mono retornado. Nao precisamos esperar pelo resultado da requisição.
             *      O Netty vai responder ao cliente que realizou a requisição quando houver o dado dentro do Mono.
             * 
             * No futuro, algum dado estará presente dentro do Mono.
             * 
             *  Dessa forma, não precisamos utilizar o método block() para bloquear a execução e esperar pelo resultado do outro servidor.
             */

}
