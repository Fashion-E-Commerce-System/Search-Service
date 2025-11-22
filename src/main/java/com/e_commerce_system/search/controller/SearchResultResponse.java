package com.e_commerce_system.search.controller;

import com.e_commerce_system.search.document.Article;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class SearchResultResponse {
    private List<Article> articles;
    private List<String> suggestions;
}