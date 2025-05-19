package com.lmg.crawler_qa_tester.service;
import com.lmg.crawler_qa_tester.constants.LinkStatusEnum;
import com.lmg.crawler_qa_tester.constants.ReportStatus;
import com.lmg.crawler_qa_tester.dto.ReportDetails;
import com.lmg.crawler_qa_tester.repository.ReportRepository;
import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import com.lmg.crawler_qa_tester.repository.entity.CrawlHeaderEntity;
import com.lmg.crawler_qa_tester.repository.internal.CrawlDetailRepository;
import com.lmg.crawler_qa_tester.repository.internal.CrawlHeaderRepository;
import com.opencsv.CSVWriter;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class ReportGeneratorConsumer {
    @Value("${env.app.location}")
    String fileLocation;
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private CrawlHeaderRepository crawlHeaderRepository;

    @Transactional
    @ServiceActivator(inputChannel = "reportProcessorChannel")
    public void generateReport(Message<ReportDetails> message)  {
        ReportDetails reportDetail = message.getPayload();
        Long reportId = reportDetail.getId();
        try {
            long count = reportRepository.countByIdAndStatus(reportId, ReportStatus.SUCCESS.getCode());
            if (count > 0) {
            return;
        }

        List<CrawlDetailEntity> crawlDetails = reportDetail.getCrawlDetailEntityList();
        if(crawlDetails.isEmpty())
        {
            throw new RuntimeException("Crawl Details is empty");
        }
            Integer crawlHeaderId = crawlDetails.get(0).getCrawlHeaderId();
            Optional<CrawlHeaderEntity> optionalCrawlHeader = crawlHeaderRepository.findById(crawlHeaderId);
            if(optionalCrawlHeader.isEmpty())
            {
                throw new RuntimeException("Crawl Header is empty");
            }
        String fromURL =    optionalCrawlHeader.get().getCompareFromBaseUrl();
        String toURL  =      optionalCrawlHeader.get().getCompareToBaseUrl();
        Map<String,CrawlDetailEntity> fromEnv = new HashMap<>();
        Map<String,CrawlDetailEntity> toEnv = new HashMap<>();

        for (CrawlDetailEntity detail : crawlDetails) {
            if (detail.getEnv().equals("FROM_ENV")) {
                fromEnv.put(detail.getPath(), detail);
            } else {
                toEnv.put(detail.getPath(), detail);
            }
        }
            String locale = reportDetail.getLocale();
            String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String country = reportDetail.getCountry();
            URL fromURLObj = new URL(fromURL);
            URL toURLObj = new URL(toURL);
            String fromURLHost = fromURLObj.getHost();
            String toURLHost = toURLObj.getHost();
            String[] fromURLParts = fromURLHost.split("\\.");
            String hostName = fromURLParts.length > 2
                    ? fromURLParts[fromURLParts.length - 2] + "." + fromURLParts[fromURLParts.length - 1]
                    : fromURLHost;
            String fromPrefix= fromURLParts.length > 2 ?fromURLParts[0]:"";
            String[] toURLParts = toURLHost.split("\\.");
            String toPrefix = toURLParts.length > 2? toURLParts[0]:"";
            String fileName = fromPrefix+"_"+toPrefix+"_"+hostName+"_"+country+"_"+locale+"_"+dateTime;
            try (CSVWriter writer = new CSVWriter(new FileWriter(fileLocation+"/"+fileName+".csv"))) {
                String[] headerBlock ={ "URL "+hostName+"_"+country+"_"+locale,
                        "Status "+fromPrefix,
                        "Status "+toPrefix,
                        "Count "+fromPrefix,
                        "Count "+toPrefix,
                        "Count Difference",
                        "Count Difference Percentage"};
                writer.writeNext(headerBlock);
                Set<String> uniquePath = new HashSet<>();
                for(CrawlDetailEntity detail:crawlDetails)
                {
                    String path = detail.getPath();
                    if(uniquePath.contains(path))
                    {
                        continue;
                    }
                    uniquePath.add(path);
                    String fromEnvStatus = fromEnv.containsKey(path)?fromEnv.get(path).getProcessFlag():LinkStatusEnum.MISSING.getValue();
                    String toEnvStatus = toEnv.containsKey(path)?toEnv.get(path).getProcessFlag(): LinkStatusEnum.MISSING.getValue();
                    int countFromEnv = fromEnv.containsKey(path) ? (fromEnv.get(path).getProductCount()!=null? fromEnv.get(path).getProductCount():0): 0;
                    int countToEnv = toEnv.containsKey(path) ? (toEnv.get(path).getProductCount()!=null? toEnv.get(path).getProductCount():0) : 0;
                    int countDifference = countFromEnv - countToEnv;
                    String countPercentage = (countToEnv != 0) ? String.format("%.2f", 100.0 * countDifference / countToEnv) : "NA";
                    writeDetailsToCsv(path, fromEnvStatus, toEnvStatus, countFromEnv, countToEnv, countDifference, countPercentage, writer);;
                }
            }

            reportRepository.updateStatusAndTimeById(reportId, ReportStatus.SUCCESS.getCode());

        } catch (Exception e) {
            e.printStackTrace();
            reportRepository.updateStatusAndTimeById(reportId, ReportStatus.ERROR.getCode());
        }
    }
    private void writeDetailsToCsv(String path, String fromEnvStatus, String toEnvStatus,
                                   int countFromEnv, int countToEnv, int countDifference, String countPercentage,
                                   CSVWriter writer) throws IOException {

        String[] record = {
                path,
                fromEnvStatus,
                toEnvStatus,
                String.valueOf(countFromEnv),
                String.valueOf(countToEnv),
                String.valueOf(countDifference),
                countPercentage
        };
        writer.writeNext(record);

    }
}
