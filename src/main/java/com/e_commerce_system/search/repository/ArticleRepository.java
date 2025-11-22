package com.e_commerce_system.search.repository;

import com.e_commerce_system.search.document.Article;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;

public interface ArticleRepository extends ReactiveElasticsearchRepository<Article, String> {
}
