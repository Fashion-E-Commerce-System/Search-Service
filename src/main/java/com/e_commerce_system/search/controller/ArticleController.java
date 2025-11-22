
package com.e_commerce_system.search.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SuggestMode;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Suggester;
import com.e_commerce_system.search.document.Article;
import com.e_commerce_system.search.repository.ArticleRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/articles")
@AllArgsConstructor
@Getter
@Setter
public class ArticleController {

    private final ArticleRepository articleRepository;
    private final ReactiveElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchClient elasticsearchClient;

    @GetMapping("/{id}")
    public Mono<Article> findById(@PathVariable String id) {
        return articleRepository.findById(id);
    }

    @GetMapping("/search")
    public Mono<SearchResultResponse> search(@RequestParam String query) {
        Suggester suggester = Suggester.of(s -> s
                .suggesters("did-you-mean", f -> f
                        .term(t -> t.field("prod_name").suggestMode(SuggestMode.Always))
                        .text(query)
                )
        );

        NativeQuery searchQuery = new NativeQueryBuilder()
                .withQuery(q -> q
                        .multiMatch(mm -> mm
                                .query(query)
                                .fields("prod_name", "message", "event.original")
                                .fuzziness("AUTO")
                                .prefixLength(1)
                                .operator(Operator.Or)
                        )
                )
                .withSuggester(suggester)
                .build();

        return elasticsearchOperations.search(searchQuery, Article.class)
                .collectList()
                .map(searchHits -> {
                    List<Article> articles = searchHits.stream()
                            .map(SearchHit::getContent)
                            .collect(Collectors.toList());
                    return new SearchResultResponse(articles, List.of());
                });
    }

    @GetMapping("/suggest")
    public Flux<String> suggest(@RequestParam String query) throws IOException {
        Suggester suggester = Suggester.of(s -> s
                .suggesters("article-suggester", f -> f
                        .completion(c -> c
                                .field("suggest")
                                .skipDuplicates(true)
                                .size(10)
                                .fuzzy(fz -> fz
                                        .fuzziness("AUTO")
                                        .minLength(2)
                                        .prefixLength(1)
                                )
                        )
                        .text(query)
                )
        );

        SearchRequest request = SearchRequest.of(r -> r
                .index("article_index_copy")
                .suggest(suggester)
        );

        SearchResponse<Void> response = elasticsearchClient.search(request, Void.class);

        List<String> suggestions = response.suggest()
                .get("article-suggester")
                .stream()
                .flatMap(s -> s.completion().options().stream())
                .map(opt -> opt.text())
                .toList();

        return Flux.fromIterable(suggestions);
    }
}