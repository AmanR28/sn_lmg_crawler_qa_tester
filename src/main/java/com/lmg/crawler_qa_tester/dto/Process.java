package com.lmg.crawler_qa_tester.dto;

import com.lmg.crawler_qa_tester.constants.ProcessStatusEnum;
import java.sql.Timestamp;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Process {
    private Integer id;
    private String prodBaseUrl;
    private String preProdBaseUrl;
    private ProcessStatusEnum status;
    private Timestamp createdTime;
    private Timestamp updatedTime;

}
