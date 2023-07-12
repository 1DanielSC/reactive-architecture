package com.example.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gateway.entity.Item;
import com.example.gateway.entity.RequestItemDTO;

@RestController
@RequestMapping("api")
public class GlobalController {
    
    @Autowired
    private StreamBridge bridge;

    @PostMapping("/order")
    public ResponseEntity<?> createOrder(@RequestBody(required = true) RequestItemDTO dto){

        Message<RequestItemDTO> message  = MessageBuilder.withPayload(dto).build();
        System.out.println("Vou enviar o evento.");
        bridge.send("order-addItemToOrder-input", message);

        return ResponseEntity.ok("Evento enviado.");
    }
}
