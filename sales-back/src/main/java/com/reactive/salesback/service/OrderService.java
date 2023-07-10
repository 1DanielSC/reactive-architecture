package com.reactive.salesback.service;

import java.time.LocalDateTime;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;

import com.reactive.salesback.exception.InvalidDataException;
import com.reactive.salesback.exception.NotFoundException;

import com.reactive.salesback.model.Item;
import com.reactive.salesback.model.Order;
import com.reactive.salesback.model.dtos.ProductDTO;
import com.reactive.salesback.model.dtos.RequestItemDTO;
import com.reactive.salesback.model.enums.EnumStatusOrder;
import com.reactive.salesback.repository.OrderRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Configuration
public class OrderService {
    
    @Autowired
    private OrderRepository repository;

    @Autowired
    private StreamBridge bridge;

    @Bean
    public Supplier<Mono<Order>> createOrder(){
        return () -> {
            Order order = new Order();
            order.setId(null);
            order.setDate(LocalDateTime.now());
            order.setStatus(EnumStatusOrder.CREATED);
            return repository.save(order);
        };
    }

    @Bean
    public Supplier<Flux<Order>> findAll(){
        return () -> repository.findAll();
    }

    /*
     * OBS.: Publiquei uma mensagem na fila relacionada ao exchange criado para este método.
     * A mensagem publicada foi printada no código abaixo.
     */
    @Bean
    public Function<Mono<String>, Mono<Order>> findById(){
        //return id -> id.flatMap(repository::findById)
        return id -> {
            System.out.println("oi");
            return id
            .doOnNext(a -> System.out.println("ID: " + a))
            .flatMap(repository::findById);
        };
    }

    @Bean
    //asynchronous communication through RabbitMQ/Kafka to Product Service
    /*
     * Problema: Esta funcao nao esta enviado msg assincrona ao Product.
     * "requestProduct().apply(requestBody)" ta retornando o proprio produto aplicado...
     */
    public Function<ProductDTO, Mono<ProductDTO>> requestProduct(){
        return product -> {
            System.out.println("Sales-Service: requestProduct");
            return Mono.just(product);
        };
    }

    @Bean
    public Function<Mono<RequestItemDTO>, Mono<Order>> addItemToOrder(){
        return dtoMono -> {
            return 
            dtoMono.flatMap(dto -> {
                
                if(dto == null)
                    System.out.println("DTO eh null");
                else{
                    System.out.println("ID: " + dto.getId());
                    System.out.println("Nome: " + dto.getItem().getName());
                    System.out.println("Quantity: " + dto.getItem().getQuantity());
                    System.out.println("Price: " + dto.getItem().getPrice());
                }
                return 
                repository.findById(dto.getId())
                .switchIfEmpty(
                    Mono.error(new NotFoundException("Order not found with this id."))
                )
                .flatMap(order -> {
                    ProductDTO requestBody = new ProductDTO();
                    requestBody.setName(dto.getItem().getName());
                    requestBody.setQuantity(dto.getItem().getQuantity());
    
                    bridge.send("requestProduct-in-0", requestBody, MimeTypeUtils.APPLICATION_JSON);
                    
                    return requestProduct().apply(requestBody)
                    .switchIfEmpty(
                        Mono.error(new InvalidDataException("Nao foi possivel obter o product."))
                    )
                    .flatMap(product -> {
                        if(product == null)
                            System.out.println("Product recebido eh null");
                        else{
                            System.out.println("Price recebido: " + product.getPrice());
                        }
    
                        Item item = dto.getItem();
                        item.setPrice(product.getPrice());                 
                        Item itemFromStream = order.getItems()
                        .stream()
                        .filter(productItem -> productItem.getName().equals(item.getName()))
                        .findFirst()
                        .orElse(new Item(item.getName(),item.getPrice()));
    
                        if(itemFromStream.getQuantity() == 0)
                            order.getItems().add(item); //TODO: Bad practice.
                        itemFromStream.setQuantity(itemFromStream.getQuantity()+item.getQuantity());
                        Double priceItem = item.getPrice()*item.getQuantity();
                        order.setTotalPrice(order.getTotalPrice()+priceItem);
                        return repository.save(order);
                    });
                });
            });
        };
    }
            

    // public Flux<Order> findAll(){
    //     return repository.findAll();
    // }

    // public Mono<Order> findById(String id){
    //     return repository.findById(id);
    // }
    
    // public Mono<Order> createOrder(){
    //     Mono<Order> order = Mono.just(new Order());
    //     return order
    //     .flatMap(e -> {
    //         e.setId(null);
    //         e.setDate(LocalDateTime.now());
    //         e.setStatus(EnumStatusOrder.CREATED);
    //         return repository.save(e);
    //     });
    // }

    // public Mono<Order> updateStatus(String id, EnumStatusOrder status){
    //     Mono<Order> order = findById(id);
    //     return order
    //     .switchIfEmpty(Mono.error(new NotFoundException("Order not found with this id.")))
    //     .flatMap(e -> {
    //         if(status == EnumStatusOrder.APPROVED)
    //             return placeOrder(order);
    //         else if(status == EnumStatusOrder.CANCELED)
    //             return cancelOrder(order);
    //         else if(status == EnumStatusOrder.REFUSED)
    //             return refuseOrder(order);
    //         else
    //             return Mono.error(new IllegalArgumentException("Invalid status."));
    //     });
    // }

    // public Mono<Order> addItemToOrder(String orderId, Item item){

    //     return repository.findById(orderId)
    //     .switchIfEmpty(Mono.error(new NotFoundException("Order not found with this id.")))
    //     .flatMap(orderMono -> {            
    //         return requestProduct(item)
    //         .switchIfEmpty(Mono.error(new InvalidDataException("Nao foi possivel obter o product.")))
    //         .flatMap(product -> {
    //             item.setPrice(product.getPrice());                 
    //             Item itemFromStream = orderMono.getItems()
    //             .stream()
    //             .filter(productItem -> productItem.getName().equals(item.getName()))
    //             .findFirst()
    //             .orElse(new Item(item.getName(),item.getPrice()));


    //             if(itemFromStream.getQuantity() == 0)
    //                 orderMono.getItems().add(item); //TODO: Bad practice.
    //             itemFromStream.setQuantity(itemFromStream.getQuantity()+item.getQuantity());
    //             Double priceItem = item.getPrice()*item.getQuantity();
    //             orderMono.setTotalPrice(orderMono.getTotalPrice()+priceItem);
    //             return repository.save(orderMono);  
    //         });

    //     });
    // }

    //TODO: Verificar como lidar com erros de requisição
    // private Mono<ProductDTO> requestProduct(Item item){
    //     return Flux.range(0, 1)
    //     .parallel()
    //     .runOn(Schedulers.boundedElastic())
    //     .flatMap(e -> requestProductOnMicroservice(item))
    //     .sequential()
    //     .next();
    // }

    // private Mono<ProductDTO> requestProductOnMicroservice(Item item){
    //     return productClient.requestProduct(item);
    // }

    


    // private Mono<Order> placeOrder(Mono<Order> order){
    //     return order
    //     .switchIfEmpty(Mono.error(new NotFoundException("Order not found.")))
    //     .flatMap(e ->{
    //         e.setStatus(EnumStatusOrder.APPROVED);
    //         return repository.save(e);
    //     });
    // }


    // private Mono<Order> cancelOrder(Mono<Order> order){
    //     return order
    //     .switchIfEmpty(Mono.error(new NotFoundException("Order not found.")))
    //     .filter(e -> e.getStatus() == EnumStatusOrder.CREATED)
    //     .switchIfEmpty(Mono.error(new PreconditionFailedException("Order status is not CREATED.")))
    //     .doOnNext(e -> updateProductQuantity(e).subscribe())
    //     .flatMap(e ->{
    //         e.setStatus(EnumStatusOrder.CANCELED);
    //         return repository.save(e);
    //     });
    // }
    // private Mono<Order> refuseOrder(Mono<Order> order){
    //     return order
    //     .switchIfEmpty(Mono.error(new NotFoundException("Order not found.")))
    //     .filter(e -> e.getStatus() == EnumStatusOrder.CREATED)
    //     .switchIfEmpty(Mono.error(new PreconditionFailedException("Order status is not CREATED.")))
    //     .doOnNext(e -> updateProductQuantity(e).subscribe())
    //     .flatMap(e -> {
    //         e.setStatus(EnumStatusOrder.REFUSED);
    //         return repository.save(e);
    //     });
    // }

    // private Mono<Item> updateProductQuantity(Order order){
    //     return Flux.range(0, 1)
    //     .parallel()
    //     .runOn(Schedulers.boundedElastic())
    //     .flatMap(e -> updateOnProductMicroservice(order)) //TODO: Changing doOnNext to flatMap fixed the request problem
    //     .sequential()
    //     .next();
    // }

    // private Flux<Item> updateOnProductMicroservice(Order order){
    //     return productClient.increaseQuantity(order.getItems());
    // }
    

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
