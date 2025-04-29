package com.lmg.crawler_qa_tester.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class Domain {
    @NonNull
    private String name;
    @NonNull
    private String baseUrl;
    private String browserType;
    private boolean browserHeadless;
    private int consumersThread;

}
