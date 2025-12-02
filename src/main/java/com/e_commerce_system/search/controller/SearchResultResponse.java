package com.e_commerce_system.search.controller;

import com.e_commerce_system.search.document.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class SearchResultResponse {
    private List<Product> products;
    private List<String> suggestions;
}