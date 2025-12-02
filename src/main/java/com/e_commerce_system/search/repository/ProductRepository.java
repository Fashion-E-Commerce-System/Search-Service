package com.e_commerce_system.search.repository;

import com.e_commerce_system.search.document.Article;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Optional;

public interface ArticleRepository extends ElasticsearchRepository<Article, String> {
    Optional<Article> findByArticleId(Long articleId);
}
