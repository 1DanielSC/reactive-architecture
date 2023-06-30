package com.reactive.reviewback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.web.reactive.config.EnableWebFlux;

@EnableWebFlux
@EnableCaching
@EnableR2dbcRepositories
@SpringBootApplication
public class ReviewBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReviewBackApplication.class, args);
	}

}
