package com.reactive.reviewback.service;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import com.reactive.reviewback.model.ProductReview;
import com.reactive.reviewback.model.Review;
import com.reactive.reviewback.model.dtos.ProductReviewDTO;
import com.reactive.reviewback.repository.ProductReviewRepository;
import com.reactive.reviewback.repository.ReviewRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
public class ProductReviewService {
    
    @Autowired
    private ProductReviewRepository repository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private StreamBridge bridge;

    @Bean
    public Supplier<Flux<ProductReview>> findAll(){
        return () -> repository.findAll();
    }

    @Bean
    public Function<Mono<String>, Mono<ProductReview>> findById(){
        return id -> {
            return id.flatMap(e -> {
                System.out.println("ID: " + e);
                return repository.findById(e);
            });
        }; 
    }

    private Mono<ProductReview> findByName(String name){
        return repository.findByName(name);
    }

    @Bean
    public Consumer<Flux<ProductReviewDTO>> registerReview(){
        return dto ->{
            dto.flatMap(productReviewDto -> {
                System.out.println("Recebi confirmação da existência do produto...");
                ProductReview productReview = new ProductReview();
                productReview.setProductName(productReviewDto.getProductName());
                productReview.setRating(productReviewDto.getRating());

                List<Review> reviews = new ArrayList<>();

                Review review = new Review();
                review.setProductName(productReviewDto.getProductName());
                review.setRating(productReviewDto.getRating());
                review.setReview(productReviewDto.getReview());

                reviews.add(review);
                productReview.setReviews(reviews);
                return repository.save(productReview);
            }).subscribe();
        };
    }

    @Bean
    public Function<Flux<Review>, Flux<ProductReview>> save(){
        return input -> {
            return 
            input.flatMap(review -> {
                return
                findByName(review.getProductName())
                .switchIfEmpty(Mono.defer(() -> {
                    ProductReviewDTO dto = new ProductReviewDTO();
                    dto.setProductName(review.getProductName());
                    dto.setRating(review.getRating());
                    dto.setReview(review.getReview());

                    System.out.println("Vou enviar mensagem para checar existencia do produto.");
                    Message<ProductReviewDTO> message = MessageBuilder.withPayload(dto).build();
                    bridge.send("confirmProductExistance-input", message);

                    return Mono.empty();
                }))
                .flatMap(productReview -> {

                    if(productReview == null)
                        return Mono.empty();
                    
                    System.out.println("Já existe um review cadastrado...");
                    return
                    reviewRepository.save(review)
                    .flatMap(reviewSaved ->{
                        productReview.addReview(reviewSaved);
                        productReview.setRating(productReview.getReviews().stream()
                            .mapToDouble(Review::getRating).sum() / productReview.getReviews().size());

                        System.out.println("Qtd de reviews: " + productReview.getReviews().size());
                        System.out.println("Novo rating: " + productReview.getRating());
                        return Mono.just(productReview);
                    })
                    .flatMap(repository::save);
                });
            });
        };
    }

}
