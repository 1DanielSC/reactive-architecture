package com.reactive.reviewback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;

@EnableWebFlux
@SpringBootApplication
public class ReviewBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReviewBackApplication.class, args);
	}

}
