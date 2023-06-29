package com.reactive.productback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;

@EnableWebFlux
@SpringBootApplication
public class ProductBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductBackApplication.class, args);
	}

}
