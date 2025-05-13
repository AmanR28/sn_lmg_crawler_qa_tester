package com.lmg.crawler_qa_tester.dto;

import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportDetails {
    private Long id;
    private List<CrawlDetailEntity> crawlDetailEntityList;
    private String locale;
    private String country;
}
