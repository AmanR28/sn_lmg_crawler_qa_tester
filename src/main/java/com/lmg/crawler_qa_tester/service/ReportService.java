package com.lmg.crawler_qa_tester.service;

import com.lmg.crawler_qa_tester.constants.EnvironmentEnum;
import com.lmg.crawler_qa_tester.constants.LinkStatusEnum;
import com.lmg.crawler_qa_tester.repository.internal.CrawlDetailRepository;
import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import com.lmg.crawler_qa_tester.util.CsvUtil;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportService {
  @Autowired CrawlDetailRepository crawlDetailRepository;

  public byte[] generateComparisonReport(Integer processId) {
    List<String> reportHeaders = List.of(new String[] {"Path", "Prod Status", "Pre Prod Status"});
    List<String[]> reportData = new LinkedList<String[]>();

    List<CrawlDetailEntity> crawlDetailEntityList =
        crawlDetailRepository.findAllByCrawlHeaderId(processId);

    List<String> uniquePaths =
        crawlDetailEntityList.stream()
            .map(CrawlDetailEntity::getPath)
            .distinct()
            .sorted()
            .collect(Collectors.toCollection(LinkedList::new));

    HashMap<String, CrawlDetailEntity> prodMap =
        new HashMap<>(
            crawlDetailEntityList.stream()
                .filter(e -> e.getEnv().equals(EnvironmentEnum.FROM_ENV.getValue()))
                .collect(Collectors.toMap(CrawlDetailEntity::getPath, e -> e)));
    HashMap<String, CrawlDetailEntity> preProdMap =
        new HashMap<>(
            crawlDetailEntityList.stream()
                .filter(e -> e.getEnv().equals(EnvironmentEnum.TO_ENV.getValue()))
                .collect(Collectors.toMap(CrawlDetailEntity::getPath, e -> e)));

    for (String path : uniquePaths) {
      reportData.add(
          new String[] {
            path,
            prodMap.get(path) != null
                ? prodMap.get(path).getProcessFlag()
                : LinkStatusEnum.MISSING.getValue(),
            preProdMap.get(path) != null
                ? preProdMap.get(path).getProcessFlag()
                : LinkStatusEnum.MISSING.getValue()
          });
    }

    return CsvUtil.getCsvData(reportHeaders, reportData);
  }
}
