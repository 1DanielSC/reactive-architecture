package com.reactive.salesback.service;

import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;

import com.reactive.salesback.exception.NotFoundException;

import com.reactive.salesback.model.Item;
import com.reactive.salesback.model.Order;
import com.reactive.salesback.model.dtos.OrderConfirmationDTO;
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
            return id.flatMap(repository::findById);
        };
    }


    private Mono<Order> findOrderById(String id){
        return repository.findById(id);
    }

    //Resposta da solicitação de produto
    /*
     * Se a resposta tiver OK, então a adiciono à ordem de compra.
     * Caso contrário, apenas ignoro. 
     */
    @Bean
    public Consumer<Mono<OrderConfirmationDTO>> confirmRequest(){
        return dto -> {
            dto.flatMap(request -> {
                System.out.println("confirmRequest: cheguei aqui");

                if(!request.isProductOK()){
                    System.err.println("Product not avaiable.");
                    return dto;
                }

                System.out.println("confirmRequest: produto disponivel! Vou adicionar a sua sacola.");
                findOrderById(request.getIdOrder())
                .flatMap(order -> {

                    ProductDTO productDTO = request.getProduct();

                    Item item = new Item();
                    item.setName(productDTO.getName());
                    item.setPrice(productDTO.getPrice());
                    item.setQuantity(productDTO.getQuantity());
              
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
                    
                    System.out.println("Valor total atualizado: " + order.getTotalPrice());

                    return repository.save(order);
                });
                return dto;
            }).subscribe();
        };
    }


    @Bean
    public Function<Mono<RequestItemDTO>, Mono<Order>> addItemToOrder(){
        return dtoMono -> {
            return 
            dtoMono.flatMap(dto -> {

                return 
                repository.findById(dto.getId())
                .switchIfEmpty(
                    Mono.error(new NotFoundException("Order not found with this id."))
                )
                .flatMap(order -> {
                    
                    ProductDTO requestBody = new ProductDTO();
                    requestBody.setName(dto.getItem().getName());
                    requestBody.setQuantity(dto.getItem().getQuantity());
                    
                    OrderConfirmationDTO body = new OrderConfirmationDTO();
                    body.setIdOrder(dto.getId());
                    body.setProduct(requestBody);

                    Message<OrderConfirmationDTO> message = MessageBuilder.withPayload(body).build();

                    //Vou enviar solicitação para requisitar o produto
                    bridge.send("entradadados", message);
                    //bridge.send("entradadados", body, MimeTypeUtils.APPLICATION_JSON);

                    //Novo código, so para retornar ao flatmap
                    return Mono.just(order);
                });
            });
        };
    }
            
}
