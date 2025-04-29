package com.lmg.crawler_qa_tester.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LinkEntity {
    private Integer id;
    private String url;
    private String processed;
    private String status;
    private String type;
}
