package com.lmg.crawler_qa_tester.dto;

import com.lmg.crawler_qa_tester.constants.ConsumerStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class Project {
    private Integer id;
    private Timestamp createdTime;
    private Timestamp updatedTime;
    private String prodBaseUrl;
    private String preProdBaseUrl;
    private ConsumerStatusEnum prodProcessStatus;
    private ConsumerStatusEnum preProdProcessStatus;

}
