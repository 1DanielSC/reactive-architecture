package com.reactive.reviewback.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
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
import reactor.core.scheduler.Schedulers;

@Service
public class ProductReviewService {
    
    @Autowired
    private ProductReviewRepository repository;

    //private ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private WebClient.Builder webClient;

    public Flux<ProductReview> findAll(){
        return repository.findAll();
    }

    public Mono<Void> deleteAll(){
        return repository.deleteAll();
    }

    public Mono<ProductReview> findById(String id){
        return repository.findById(id);
    }

    public Mono<ProductReview> findByName(String name){
       // return repository.findByName(name).publishOn(Schedulers.fromExecutor(executorService));
       return Mono.defer(() -> repository.findByName(name))
       .publishOn(Schedulers.boundedElastic());
       //.publishOn(Schedulers.fromExecutorService(executorService));
        
    }

    public Mono<ProductReview> save(Review review){
        return 
        findByName(review.getProductName())
        .switchIfEmpty(Mono.defer(() -> {
            return findProduct(review)
            .switchIfEmpty(Mono.error(new GenericException("No product found with this name.")))
            .flatMap(e -> {
                ProductReview newProductReview = new ProductReview();
                newProductReview.setProductName(review.getProductName());
                return Mono.just(newProductReview);
            });
        }))
        .zipWith(reviewRepository.save(review), (productReviewUpdated, reviewSaved) -> {
            productReviewUpdated.addReview(reviewSaved);
            productReviewUpdated.setRating(productReviewUpdated.getReviews().stream()
                .mapToDouble(Review::getRating).sum() / productReviewUpdated.getReviews().size());
            return productReviewUpdated;
        })
        .flatMap(repository::save);
    }


    private Mono<ProductDTO> findProduct(Review review){
        return Flux.range(0, 1)
        .parallel()
        .runOn(Schedulers.boundedElastic())
        //.runOn(Schedulers.fromExecutor(executorService))
        .flatMap(e -> requestOnMicroservice(review))
        .sequential()
        .next();
    }

    private Mono<ProductDTO> requestOnMicroservice(Review review){
        Mono<ProductDTO> productRequest =  webClient.build()
        .get()
        .uri("/product/name/{name}", review.getProductName())
        .retrieve()
        .onStatus(status -> status.value() == HttpStatus.SERVICE_UNAVAILABLE.value(),
            response -> Mono.error(new APIConnectionError("Connection to product-back has failed.")))
        .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(),
            response -> Mono.error(new NotFoundException("Product not found.")))
        .onStatus(status -> status.value() != HttpStatus.OK.value(),
            response -> Mono.error(new GenericException("Error on request to product-back server.")))
        .bodyToMono(ProductDTO.class);
        productRequest.subscribe();
        return productRequest;
    }

}
