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
@Document(indexName = "product_index_copy")
@NoArgsConstructor
public class Product {
    @Id
    private String id;

    @Field(type = FieldType.Long, name = "product_id")
    private Long productId;

    @Field(type = FieldType.Text, name = "prod_name")
    private String prodName;

    @Field(type = FieldType.Text, name = "message")
    private String message;

    @Field(type = FieldType.Text, name = "event.original")
    private String eventOriginal;
}