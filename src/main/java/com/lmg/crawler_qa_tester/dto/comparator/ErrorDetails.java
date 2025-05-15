package com.lmg.crawler_qa_tester.dto.comparator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;

@Builder
@JsonPropertyOrder({"errorCode", "errorName", "errorMessage"})
public record ErrorDetails(
    @JsonProperty("errorCode")
    String errorCode,
    
    @JsonProperty("errorMessage")
    String errorMessage,
    
    @JsonProperty("errorName")
    String errorName
) {}
