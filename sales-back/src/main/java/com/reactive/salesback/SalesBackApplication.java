package com.reactive.salesback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@EnableCaching
@EnableR2dbcRepositories
@SpringBootApplication
public class SalesBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalesBackApplication.class, args);
	}

}
