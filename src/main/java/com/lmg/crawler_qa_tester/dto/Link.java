package com.lmg.crawler_qa_tester.dto;

import com.lmg.crawler_qa_tester.constants.LinkStatus;
import lombok.Data;

@Data
public class Link {
    private Integer projectId;
    private String url;
    private LinkStatus prodStatus;
    private LinkStatus preProdStatus;
    private String prodProcessedFlag;
    private String preProdProcessedFlag;
}
