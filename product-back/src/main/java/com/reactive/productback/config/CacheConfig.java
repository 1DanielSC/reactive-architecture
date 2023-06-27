package com.reactive.productback.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {
    
    @Bean
    public RedissonReactiveClient getClient(){
        Config config = new Config();
        config.useSingleServer()
            .setAddress("redis://127.0.0.1:6379");
            //.setPassword("admin");
        return Redisson.create(config).reactive();
    }
}
