package com.lmg.crawler_qa_tester.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Domain {
    String name;
    String baseUrl;
    boolean browserHeadless;
    String browserType;
    int consumerThread;
}
