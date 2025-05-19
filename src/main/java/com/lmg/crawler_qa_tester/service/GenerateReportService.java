package com.lmg.crawler_qa_tester.service;
import com.lmg.crawler_qa_tester.constants.ReportStatus;
import com.lmg.crawler_qa_tester.dto.GenerateReportRequest;
import com.lmg.crawler_qa_tester.repository.ReportRepository;
import com.lmg.crawler_qa_tester.repository.entity.CrawlHeaderEntity;
import com.lmg.crawler_qa_tester.repository.entity.ReportEntity;
import com.lmg.crawler_qa_tester.repository.internal.CrawlHeaderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class GenerateReportService {

    @Autowired
    private CrawlHeaderRepository crawlHeaderRepository;

    @Autowired
    private ReportRepository reportRepository;

    public Pair<String,String> generateReport(GenerateReportRequest reportRequest) {
        try {
            String domain = reportRequest.getHost();
            String country = reportRequest.getCountry();
            String locale = reportRequest.getLocale();
            String department = reportRequest.getDepartment();
            Optional<Integer> optionalId = crawlHeaderRepository.findCrawlHeaderId(domain, country, locale, department);
            if (optionalId.isEmpty()) {
                return Pair.of("Not Found", "null");
            }
            Optional<ReportEntity> existingReport = reportRepository.findExistingReport( domain, country,locale, department);

            if (existingReport.isPresent()) {
                return Pair.of("Already Exists", existingReport.get().getId().toString());
            }

            ReportEntity reportEntity = ReportEntity.builder()
                    .host(domain)
                    .status(ReportStatus.NOT_AVAILABLE.getCode())
                    .country(country)
                    .locale(locale)
                    .crawlId(Integer.valueOf(optionalId.get()))
                    .department(department)
                    .build();

            ReportEntity savedReport  = reportRepository.save(reportEntity);

            return Pair.of("OK",savedReport.getId().toString());

        } catch (Exception e) {
            e.printStackTrace();
            return Pair.of("Error", "null");
        }
    }
}
