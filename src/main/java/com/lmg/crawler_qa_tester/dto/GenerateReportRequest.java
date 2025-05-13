package com.lmg.crawler_qa_tester.dto;

import lombok.Data;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;
@Data
public class GenerateReportRequest {
    @NotBlank
    private String host;
    @NotBlank
    private String country;
    @NotBlank
    private String locale;
    @NotBlank
    private String department;
}
