package com.e_commerce_system.search.repository;

import com.e_commerce_system.search.document.Product;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Mono;

public interface ProductRepository extends ReactiveElasticsearchRepository<Product, String> {
    Mono<Product> findByProductId(Long productId);
}
