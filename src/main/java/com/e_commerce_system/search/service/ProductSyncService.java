package com.e_commerce_system.search.service;

import com.e_commerce_system.search.document.Product;
import com.e_commerce_system.search.inbox.Inbox;
import com.e_commerce_system.search.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;


@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSyncService {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void syncProduct(Inbox inboxEvent) throws JsonProcessingException {
        String eventType = inboxEvent.getEventType();
        String aggregateId = inboxEvent.getAggregateId();
        JsonNode payload = objectMapper.readTree(inboxEvent.getPayload());

        switch (eventType.trim()) {
            case "PRODUCT_CREATED":
                Product newProduct = new Product();
                newProduct.setProductId(payload.get("id").asLong());
                newProduct.setProdName(payload.get("name").asText());
                productRepository.save(newProduct).block();
                log.info("Created new product from event {}: {}", inboxEvent.getEventId(), newProduct.getProductId());
                break;

            case "PRODUCT_UPDATED":
                Long productIdToUpdate = payload.get("id").asLong();
                productRepository.findByProductId(productIdToUpdate)
                        .flatMap(product -> {
                            product.setProdName(payload.get("name").asText());
                            return productRepository.save(product);
                        })
                        .doOnSuccess(product -> {
                            if (product != null) {
                                log.info("Updated product {} from event {}", product.getProductId(), inboxEvent.getEventId());
                            }
                        })
                        .switchIfEmpty(Mono.defer(() -> {
                            log.warn("Product with id {} not found for update event {}. Creating it.", productIdToUpdate, inboxEvent.getEventId());
                            Product productToCreate = new Product();
                            productToCreate.setProductId(payload.get("id").asLong());
                            productToCreate.setProdName(payload.get("name").asText());
                            return productRepository.save(productToCreate)
                                    .doOnSuccess(createdProduct -> log.info("Created new product {} from a PRODUCT_UPDATED event", createdProduct.getProductId()));
                        })).block();
                break;

            case "PRODUCT_DELETED":
                long productIdToDelete = Long.parseLong(aggregateId);
                productRepository.findByProductId(productIdToDelete)
                        .flatMap(product -> productRepository.delete(product).thenReturn(product))
                        .doOnSuccess(product -> {
                            if (product != null) {
                                log.info("Deleted product {} from event {}", product.getProductId(), inboxEvent.getEventId());
                            }
                        }).block();
                break;

            default:
                log.warn("Unknown event type: {}", eventType);
        }
    }
}
