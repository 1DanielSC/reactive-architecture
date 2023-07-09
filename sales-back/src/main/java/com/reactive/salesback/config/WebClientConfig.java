package com.reactive.salesback.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.reactive.salesback.service.request.ProductServiceClient;

@Configuration
public class WebClientConfig {
    
    //@Autowired
    //private ReactorLoadBalancerExchangeFilterFunction lbFunction;

    @Bean
    public WebClient webClientBuilder1(){
        return WebClient.builder()
        .baseUrl("http://REACTIVEPRODUCT")
        //.filter(lbFunction)
        .build();
    }

    // @Bean
    // public ProductServiceClient productClient(WebClient webClient){
    //     HttpServiceProxyFactory factory = HttpServiceProxyFactory
    //         .builder(WebClientAdapter.forClient(webClient))
    //         .build();

    //     return factory.createClient(ProductServiceClient.class);
    // }
}
