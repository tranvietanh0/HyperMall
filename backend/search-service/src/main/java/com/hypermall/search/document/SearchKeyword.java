package com.hypermall.search.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "search_keywords")
public class SearchKeyword {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String keyword;

    @Field(type = FieldType.Long)
    private Long searchCount;

    @Field(type = FieldType.Long)
    private Long clickCount;

    @Field(type = FieldType.Date)
    private LocalDateTime lastSearchedAt;

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;
}
