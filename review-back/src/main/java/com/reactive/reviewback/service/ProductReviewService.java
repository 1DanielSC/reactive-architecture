package com.reactive.reviewback.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.reactive.reviewback.exception.APIConnectionError;
import com.reactive.reviewback.exception.GenericException;
import com.reactive.reviewback.exception.NotFoundException;
import com.reactive.reviewback.model.ProductReview;
import com.reactive.reviewback.model.Review;
import com.reactive.reviewback.model.dtos.ProductDTO;
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
    private WebClient.Builder webClient;

    @Value("${product-back.address}")
    private String productBackAddress;

    public Flux<ProductReview> findAll(){
        return repository.findAll();
    }

    public Mono<ProductReview> findById(String id){
        return repository.findById(id);
    }

    public Mono<ProductReview> findByName(String name){
        return repository.findByProductName(name);
    }

    public Mono<ProductReview> save(Review review){
        Mono<ProductReview> productReview = findByName(review.getProductName());
        return productReview
        .switchIfEmpty(Mono.defer(() -> {

            Mono<ProductDTO> productRequest =  webClient.build()
            .get()
            .uri(productBackAddress+"/product/name/{name}", review.getProductName())
            .retrieve()
            .onStatus(status -> status.value() == HttpStatus.SERVICE_UNAVAILABLE.value(),
                response -> Mono.error(new APIConnectionError("Connection to product-back has failed.")))
            .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(),
                response -> Mono.error(new NotFoundException("Product not found.")))
            .onStatus(status -> status.value() != HttpStatus.OK.value(),
                response -> Mono.error(new GenericException("Error on request to product-back server.")))
            .bodyToMono(ProductDTO.class);

            return productRequest.switchIfEmpty(Mono.error(new GenericException("No product.")))
            .flatMap(e -> {
                ProductReview newProductReview = new ProductReview();
                newProductReview.setProductName(review.getProductName());
                return Mono.just(newProductReview);
            });
        })).zipWith(reviewRepository.save(review), (productReviewUpdated, reviewSaved) -> {
            productReviewUpdated.addReview(reviewSaved);
            productReviewUpdated.setRating(productReviewUpdated.getReviews().stream()
                .mapToDouble(Review::getRating).sum() / productReviewUpdated.getReviews().size());
            return productReviewUpdated;
        })
        .flatMap(repository::save);
    }

}
