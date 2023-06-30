package com.reactive.salesback.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.reactive.salesback.model.ItemR2DBC;

@Repository
public interface ItemRepositoryR2DBC extends R2dbcRepository<ItemR2DBC, Long>{
    
}
