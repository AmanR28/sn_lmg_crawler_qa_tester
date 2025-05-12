package com.lmg.crawler_qa_tester.dto.comparator;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.lang.NonNull;

@Data
public class CompareRequest {
    @NonNull
    @JsonProperty("env_from")
    public String envFrom;
    @NonNull
    @JsonProperty("env_to")
    public String envTo;
    @NonNull
    @JsonProperty("country")
    public String country;
    @NonNull
    @JsonProperty("concept")
    public String concept;
}
