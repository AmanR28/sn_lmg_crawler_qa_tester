package com.lmg.crawler_qa_tester.dto.comparator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@Builder
@JsonPropertyOrder({"errorCode", "errorName", "errorMessage"})
public class ErrorDetails implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @JsonProperty("errorCode")
    private String errorCode;
    @JsonProperty("errorMessage")
    private String errorMessage;
    @JsonProperty("errorName")
    private String errorName;
}
