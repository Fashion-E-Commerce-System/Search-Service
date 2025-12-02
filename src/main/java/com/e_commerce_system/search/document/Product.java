package com.e_commerce_system.search.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;


@Setting(settingPath = "es-settings.json")
@Getter
@Setter
@Document(indexName = "article_index_copy")
@NoArgsConstructor
public class Article {
    @Id
    private String id;

    @Field(type = FieldType.Long, name = "article_id")
    private Long articleId;

    @Field(type = FieldType.Text, name = "prod_name")
    private String prodName;

    @Field(type = FieldType.Text, name = "message")
    private String message;

    @Field(type = FieldType.Text, name = "event.original")
    private String eventOriginal;
}