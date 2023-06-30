package com.reactive.salesback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.web.reactive.config.EnableWebFlux;

@EnableWebFlux
@EnableCaching
@SpringBootApplication
public class SalesBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalesBackApplication.class, args);
	}

}
