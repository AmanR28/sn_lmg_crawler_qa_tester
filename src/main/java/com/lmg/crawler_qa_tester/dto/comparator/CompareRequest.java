package com.lmg.crawler_qa_tester.dto.comparator;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.lang.NonNull;

@Data
public class CompareRequest {
    @NonNull
    @JsonProperty("first_env")
    public String firstEnv;
    @NonNull
    @JsonProperty("second_env")
    public String secondEnv;
    @NonNull
    @JsonProperty("country")
    public String country;
    @NonNull
    @JsonProperty("concept")
    public String concept;
}
