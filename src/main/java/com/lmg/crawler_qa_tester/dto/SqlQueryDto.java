package com.lmg.crawler_qa_tester.dto;

import lombok.Data;

/**
 * DTO for SQL query customization
 */
@Data
public class SqlQueryDto {
    private String selectSql;
    private String updateSql;
}
