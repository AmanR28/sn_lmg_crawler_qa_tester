package com.lmg.crawler_qa_tester.service;
import com.lmg.crawler_qa_tester.constants.EnvironmentEnum;
import com.lmg.crawler_qa_tester.repository.CrawlDetailRepository;
import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import com.lmg.crawler_qa_tester.util.WriteComparisonReportToCSV;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

import java.util.*;

@Service
public class CompareLinkService {
    @Autowired
    private CrawlDetailRepository crawlDetailRepository;
    public byte[]  getCompareReports(Integer processId)
    {
      List<CrawlDetailEntity> crawlDetailEntityList =   crawlDetailRepository.findAllByCrawlHeaderId(processId);
        Map<String,CrawlDetailEntity> prodSet = new HashMap<>();
        Map<String,CrawlDetailEntity>preProdSet = new HashMap<>();
        if(crawlDetailEntityList.isEmpty())
         {
          throw new RuntimeException("No crawl details found for the provided id.");
         }
        for (CrawlDetailEntity detail : crawlDetailEntityList) {
            if (detail.getEnv().equals(EnvironmentEnum.PROD.getValue())) {
                prodSet.put(detail.getPath(), detail);
            } else {
                preProdSet.put(detail.getPath(), detail);
            }
        }
        try {
            WriteComparisonReportToCSV comparisonReportToCSV = new WriteComparisonReportToCSV();
            return comparisonReportToCSV.writeMissingReportToCSV(crawlDetailEntityList,prodSet,preProdSet);

        } catch (IOException e) {
            throw new RuntimeException("Error generating CSV: " + e.getMessage(), e);
        }

    }

}
