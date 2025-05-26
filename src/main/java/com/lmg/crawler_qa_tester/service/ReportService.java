package com.lmg.crawler_qa_tester.service;

import com.lmg.crawler_qa_tester.constants.ReportStatus;
import com.lmg.crawler_qa_tester.dto.GenerateReportRequest;
import com.lmg.crawler_qa_tester.repository.CrawlRepository;
import com.lmg.crawler_qa_tester.repository.ReportRepository;
import com.lmg.crawler_qa_tester.repository.entity.CrawlHeaderEntity;
import com.lmg.crawler_qa_tester.repository.entity.ReportEntity;
import com.lmg.crawler_qa_tester.repository.internal.CrawlHeaderRepository;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
public class ReportService {
  @Autowired CrawlRepository crawlRepository;
  @Autowired ReportRepository reportRepository;
  @Autowired private CrawlHeaderRepository crawlHeaderRepository;

  public Pair<String, String> generateReport(GenerateReportRequest reportRequest) {
    try {

      Integer crawlHeaderId = reportRequest.getCrawlHeaderId();
      Optional<CrawlHeaderEntity> optionalId = crawlHeaderRepository.findById(crawlHeaderId);

      if (optionalId.isEmpty()) {
        return Pair.of("Not Found", "null");
      }
      CrawlHeaderEntity entity = optionalId.get();
      String domain = entity.getDomain();
      String country = entity.getCountry();
      String locale = entity.getLocale();
      String department = entity.getDepartment();
      Optional<ReportEntity> existingReport = reportRepository.findByCrawlId(crawlHeaderId);

      if (existingReport.isPresent()) {
        return Pair.of("Already Exists", existingReport.get().getId().toString());
      }

      ReportEntity reportEntity =
          ReportEntity.builder()
              .host(domain)
              .status(ReportStatus.NOT_AVAILABLE.getCode())
              .country(country)
              .locale(locale)
              .crawlId(crawlHeaderId)
              .department(department)
              .build();

      ReportEntity savedReport = reportRepository.save(reportEntity);

      return Pair.of("OK", savedReport.getId().toString());

    } catch (Exception e) {
      e.printStackTrace();
      return Pair.of("Error", "null");
    }
  }

  public Optional<ReportEntity> getReportStatus(Integer id) {
    return reportRepository.findById(id);
  }
}
