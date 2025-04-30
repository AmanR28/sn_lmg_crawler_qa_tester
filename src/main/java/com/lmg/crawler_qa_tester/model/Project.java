package com.lmg.crawler_qa_tester.model;

import java.sql.Timestamp;

// Metadata for Project
public class Project {
    // Primary Key
    private Integer id;

    private Timestamp createdTime;
    private Timestamp updatedTime;

    // Report Meta
    private String csvReport;
    private String excelReport;

    // Process Meta
    private Integer prodConsumerCount;
    private Integer preProdConsumerCount;
}
