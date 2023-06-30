package com.reactive.salesback.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.reactive.salesback.model.OrderR2DBC;

@Repository
public interface OrderRepositoryR2DBC extends R2dbcRepository<OrderR2DBC, Long>{
    
}
