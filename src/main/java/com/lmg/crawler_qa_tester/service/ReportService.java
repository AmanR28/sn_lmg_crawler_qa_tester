package com.lmg.crawler_qa_tester.service;

import com.lmg.crawler_qa_tester.constants.EnvironmentEnum;
import com.lmg.crawler_qa_tester.constants.LinkStatusEnum;
import com.lmg.crawler_qa_tester.dto.Link;
import com.lmg.crawler_qa_tester.repository.CrawlRepository;
import com.lmg.crawler_qa_tester.util.CsvUtil;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportService {
  @Autowired CrawlRepository crawlRepository;

  public byte[] generateComparisonReport(Integer processId) {
    List<String> reportHeaders = List.of(new String[] {"Path", "Prod Status", "Pre Prod Status"});
    List<String[]> reportData = new LinkedList<String[]>();

    List<Link> links = crawlRepository.getLinksByProcessId(processId);

    List<String> uniquePaths =
        links.stream()
            .map(Link::getPath)
            .distinct()
            .sorted()
            .collect(Collectors.toCollection(LinkedList::new));

    HashMap<String, Link> prodMap =
        new HashMap<>(
            links.stream()
                .filter(e -> e.getEnv().equals(EnvironmentEnum.FROM_ENV.getValue()))
                .collect(Collectors.toMap(Link::getPath, e -> e)));
    HashMap<String, Link> preProdMap =
        new HashMap<>(
            links.stream()
                .filter(e -> e.getEnv().equals(EnvironmentEnum.TO_ENV.getValue()))
                .collect(Collectors.toMap(Link::getPath, e -> e)));

    for (String path : uniquePaths) {
      reportData.add(
          new String[] {
            path,
            prodMap.get(path) != null
                ? prodMap.get(path).getProcessFlag().getValue()
                : LinkStatusEnum.MISSING.getValue(),
            preProdMap.get(path) != null
                ? preProdMap.get(path).getProcessFlag().getValue()
                : LinkStatusEnum.MISSING.getValue()
          });
    }

    return CsvUtil.getCsvData(reportHeaders, reportData);
  }
}
