package com.lmg.crawler_qa_tester.dto.comparator;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CompareRequest(
    @NotBlank(message = "First environment cannot be null or blank")
    @JsonProperty("compare_env_from")
    String compareEnvFrom,

    @NotBlank(message = "Second environment cannot be null or blank")
    @JsonProperty("compare_env_to")
    String compareEnvTo,

    @NotBlank(message = "Country cannot be null or blank")
    @Pattern(regexp = "^[a-zA-Z]{3}$", message = "Country must be a 2-letter code")
    @JsonProperty("country")
    String country,

    @NotBlank(message = "Concept cannot be null or blank")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Concept must contain only letters, numbers, underscores, and hyphens")
    @JsonProperty("concept")
    String concept
) {}
