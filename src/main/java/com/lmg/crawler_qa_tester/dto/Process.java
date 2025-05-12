package com.lmg.crawler_qa_tester.dto;

import com.lmg.crawler_qa_tester.constants.ProcessStatusEnum;
import java.sql.Timestamp;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Process {
    private Integer id;
    private String compareFromBaseUrl;
    private String compareToBaseUrl;
    private ProcessStatusEnum status;
    private String domain;
    private String country;
    private String locale;
    private String department;
    private Integer consumerThread;
    private int pageCount;
    private Timestamp createdTime;
}
