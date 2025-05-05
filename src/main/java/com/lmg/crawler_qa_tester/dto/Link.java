package com.lmg.crawler_qa_tester.dto;

import com.lmg.crawler_qa_tester.constants.EnvironmentEnum;
import com.lmg.crawler_qa_tester.constants.LinkStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Link {
    private Integer id;
    private Integer crawlHeaderId;
    private EnvironmentEnum env;
    private String baseUrl;
    private String path;
    private LinkStatus processFlag;
    private String errorMessage;

}
