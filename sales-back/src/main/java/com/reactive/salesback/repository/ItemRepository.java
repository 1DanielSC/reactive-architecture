package com.reactive.salesback.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.reactive.salesback.model.Item;

@Repository
public interface ItemRepository extends ReactiveMongoRepository<Item, String>{
    
}
