package com.lmg.crawler_qa_tester.model;

import java.sql.Timestamp;

public class Project {
    // Primary Key
    private Integer id;

    private Timestamp createdTime;
    private Timestamp updatedTime;

    // Report Meta
    private String csvReport;
    private String excelReport;

    // Prod Meta
    private String prodBaseUrl;
    private Integer prodConsumerCount;

    // Pre Prod Meta
    private String preProdBaseUrl;
    private Integer preProdConsumerCount;
}
