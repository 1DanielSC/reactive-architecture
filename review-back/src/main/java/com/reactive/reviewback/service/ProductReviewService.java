package com.reactive.reviewback.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RBlockingDequeReactive;
import org.redisson.api.RLocalCachedMapReactive;
import org.redisson.api.RMapCacheReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.TypedJsonJacksonCodec;
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

    @Autowired
    private ProductServiceResilience productClient;

    private RBlockingDequeReactive<String> msgQueue;

    private RMapCacheReactive<String, ProductReview> productReviewCache;
    private RMapCacheReactive<String, Review> reviewCache;

    // private RLocalCachedMapReactive<String, ProductReview> productReviewCache;
    // private RLocalCachedMapReactive<String, Review> reviewCache;

    public ProductReviewService(RedissonReactiveClient client) {
        this.msgQueue = client.getBlockingDeque("mensageria", StringCodec.INSTANCE);


        this.productReviewCache = client.getMapCache("/product-review/", new TypedJsonJacksonCodec(String.class, ProductReview.class));
        this.reviewCache = client.getMapCache("/review/", new TypedJsonJacksonCodec(String.class, ProductReview.class));
        
        // LocalCachedMapOptions<String, ProductReview> mapOptionsPR = LocalCachedMapOptions.<String, ProductReview>defaults()
		// 		.syncStrategy(LocalCachedMapOptions.SyncStrategy.UPDATE)
		// 		.reconnectionStrategy(LocalCachedMapOptions.ReconnectionStrategy.CLEAR);

        // LocalCachedMapOptions<String, Review> mapOptionsR = LocalCachedMapOptions.<String, Review>defaults()
		// 		.syncStrategy(LocalCachedMapOptions.SyncStrategy.UPDATE)
		// 		.reconnectionStrategy(LocalCachedMapOptions.ReconnectionStrategy.CLEAR);

        // this.productReviewCache = client.getLocalCachedMap("/product-review/", 
        // new TypedJsonJacksonCodec(String.class, ProductReview.class),
        // mapOptionsPR);

        // this.reviewCache = client.getLocalCachedMap("/review/", 
        // new TypedJsonJacksonCodec(String.class, Review.class),
        // mapOptionsR);

    }

    public Flux<ProductReview> findAll(){
        return repository.findAll();
    }

    public Mono<Void> deleteAll(){
        productReviewCache.delete().subscribe();
        reviewCache.delete().subscribe();
        return repository.deleteAll();
    }

    public Mono<ProductReview> findById(String id){

        return productReviewCache.get("review:"+id)
            .switchIfEmpty(
                repository.findById(id)
                .doOnNext(e -> System.out.println("FindById: vou salvar no cache..."))
                .flatMap(c -> productReviewCache.fastPut("review:"+c.getId(), c)
                                            .thenReturn(c))
            );

        //return repository.findById(id);
    }

    public Mono<ProductReview> findByName(String name){
        return Mono.defer(() -> findByNameCached(name))
       .publishOn(Schedulers.boundedElastic());
       //.publishOn(Schedulers.fromExecutorService(executorService));
    }

    public Mono<ProductReview> findByNameCached(String name){
        return productReviewCache.get("review:"+name)
        .switchIfEmpty(repository.findByName(name).doOnNext(e -> System.out.println("Vou buscar no banco..."))
        .flatMap(e -> productReviewCache.fastPut("review:"+name, e).thenReturn(e)));
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
            updateOrSaveOnCache(reviewSaved).subscribe();
            productReviewUpdated.addReview(reviewSaved);
            productReviewUpdated.setRating(productReviewUpdated.getReviews().stream()
                .mapToDouble(Review::getRating).sum() / productReviewUpdated.getReviews().size());
            return productReviewUpdated;
        })
        .flatMap(repository::save)
        .flatMap(this::updateOrSaveOnCache)
        .publishOn(Schedulers.boundedElastic())
        .doOnSuccess(e -> {
            msgQueue.add("O produto " + e.getProductName() + " foi avaliado!\n"
            + "Nota atualizada: " + e.getRating())
            .subscribe();
        })
        .doOnNext(e -> printConfirmationMessage());
    }

    private void printConfirmationMessage(){
        msgQueue.takeElements()
        .doOnNext(e -> System.out.println("\nMensagem:" + e))
        .doOnError(System.out::println)
        .subscribe();
    }

    private Mono<ProductReview> updateOrSaveOnCache(ProductReview entity){
        return productReviewCache.fastPut("product-review:"+entity.getProductName(), entity)                  
        .thenReturn(entity)          
        .flatMap(e -> productReviewCache.fastPut("product-review:"+e.getId(), e).thenReturn(e))              
        .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<Review> updateOrSaveOnCache(Review entity){
        return reviewCache.fastPut("review:"+entity.getProductName(), entity)                  
        .thenReturn(entity)          
        .flatMap(e -> reviewCache.fastPut("review:"+e.getId(), e).thenReturn(e))              
        .subscribeOn(Schedulers.boundedElastic());
    }


    private Mono<ProductDTO> findProduct(Review review){
        return Flux.range(0, 1)
        .parallel()
        .runOn(Schedulers.boundedElastic())
        //.runOn(Schedulers.fromExecutor(executorService))
        .flatMap(e -> checkProductExistence(review))
        .sequential()
        .next();
    }

    private Mono<ProductDTO> checkProductExistence(Review review){
        return productClient.findByName(review.getProductName());
    }

    private Mono<ProductDTO> oldRequestOnMicroservice(Review review){
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
