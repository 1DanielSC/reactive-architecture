package com.example.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gateway.entity.RequestItemDTO;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private StreamBridge bridge;

    @PostMapping
    public Mono<String> createOrder(@RequestBody(required = true) RequestItemDTO dto){

        Message<RequestItemDTO> message  = MessageBuilder.withPayload(dto).build();
        System.out.println("Vou enviar um evento para requisitar um produto.");
        bridge.send("order-addItemToOrder-input", message);

        return Mono.just("Evento enviado.");
    }
}
