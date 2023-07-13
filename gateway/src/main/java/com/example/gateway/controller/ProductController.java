package com.example.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gateway.entity.ProductDTO;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("product")
public class ProductController {
    
    @Autowired
    private StreamBridge bridge;

    @PostMapping
    public Mono<String> createProduct(@RequestBody(required = true) ProductDTO dto){

        Message<ProductDTO> message = MessageBuilder.withPayload(dto).build();
        System.out.println("Vou enviar um evento para criar o produto.");
        bridge.send("product-save-input", message);

        return Mono.just("Evento enviado.");
    }
}
