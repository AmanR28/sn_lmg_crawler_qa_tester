package com.lmg.crawler_qa_tester.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class Domain {
    @NonNull
    String name;
    String baseUrl;
    Boolean browserHeadless;
    String browserType;
    Integer consumerThread;
    Integer pollerRate;
}
