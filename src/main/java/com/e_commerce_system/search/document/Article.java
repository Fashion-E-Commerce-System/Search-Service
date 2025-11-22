package com.e_commerce_system.search.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Document(indexName = "articles")
@Setting(settingPath = "es-settings.json")
@Getter
@Setter
@NoArgsConstructor
public class Article {

    @Id
    private String id;
    @Field(type = FieldType.Text, analyzer = "rebuilt_english", searchAnalyzer = "rebuilt_english")
    private String title;
    @Field(type = FieldType.Text, analyzer = "rebuilt_english", searchAnalyzer = "rebuilt_english")
    private String content;
    @CompletionField(analyzer = "autocomplete_analyzer", searchAnalyzer = "autocomplete_analyzer")
    private String suggest;


}
