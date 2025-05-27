package com.lmg.crawler_qa_tester.service;
import com.lmg.crawler_qa_tester.constants.LinkStatusEnum;
import com.lmg.crawler_qa_tester.constants.PageTypeEnum;
import com.lmg.crawler_qa_tester.constants.ReportStatus;
import com.lmg.crawler_qa_tester.dto.ReportDetails;
import com.lmg.crawler_qa_tester.repository.ReportRepository;
import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import com.lmg.crawler_qa_tester.repository.entity.CrawlHeaderEntity;
import com.lmg.crawler_qa_tester.repository.internal.CrawlHeaderRepository;
import com.lmg.crawler_qa_tester.util.UrlUtil;
import com.opencsv.CSVWriter;
import jakarta.transaction.Transactional;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

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
            String toURL = optionalCrawlHeader.get().getCompareToBaseUrl();
            if(toURL!=null)
            {
                reportForCompareEnv(optionalCrawlHeader,crawlDetails,reportDetail,reportId);
            }
            else
            {
                reportForEnv(optionalCrawlHeader,crawlDetails,reportDetail,reportId);
            }

        } catch (Exception e) {
            e.printStackTrace();
            updateStatus(reportId,ReportStatus.ERROR.getCode());
        }
    }
    private void reportForEnv(Optional<CrawlHeaderEntity> optionalCrawlHeader,List<CrawlDetailEntity> crawlDetails,ReportDetails reportDetail, Long reportId)
    {
        try {
            String fromURL = optionalCrawlHeader.get().getCompareFromBaseUrl();
            String locale = reportDetail.getLocale();
            String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String country = reportDetail.getCountry();
            URL fromURLObj = new URL(fromURL);
            String fromURLHost = fromURLObj.getHost();
            String[] fromURLParts = fromURLHost.split("\\.");
            String hostName = fromURLParts.length > 2
                    ? fromURLParts[fromURLParts.length - 2] + "." + fromURLParts[fromURLParts.length - 1]
                    : fromURLHost;
            String fromPrefix = fromURLParts.length > 2 ? fromURLParts[0] : "";
            String fileName = fromPrefix + "_" + hostName + "_" + country + "_" + locale + "_" + dateTime;
            try (CSVWriter writer = new CSVWriter(new FileWriter(fileLocation + "/" + fileName + ".csv"))) {
                String[] headerBlock = {"URL " + hostName + "_" + country + "_" + locale,
                        "Parent Path",
                        "Status " ,
                        "Count " ,
                       };
                writer.writeNext(headerBlock);
                Set<String> uniquePath = new HashSet<>();
                for (CrawlDetailEntity detail : crawlDetails) {
                    String path = detail.getPath();
                    if (uniquePath.contains(path)) {
                        continue;
                    }
                    uniquePath.add(path);
                    String status = detail.getProcessFlag();
                    String parentPath = detail.getParentPath();
                    String count="";
                    PageTypeEnum pageType = UrlUtil.getPageType(path);
                    if(pageType == PageTypeEnum.CATEGORY || pageType == PageTypeEnum.SEARCH)
                    {
                        if (status.equals(LinkStatusEnum.SUCCESS.getValue())) {
                          count = detail.getProductCount() != null ? String.valueOf(detail.getProductCount()) : "0";
                        }
                    }
                    String[] record = {
                            path,
                            parentPath,
                            status,
                            count
                    };
                    writer.writeNext(record);
                }
                updateStatus(reportId,ReportStatus.SUCCESS.getCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            updateStatus(reportId,ReportStatus.ERROR.getCode());

        }

    }
    private void reportForCompareEnv(Optional<CrawlHeaderEntity> optionalCrawlHeader,List<CrawlDetailEntity> crawlDetails,ReportDetails reportDetail, Long reportId)
    {
        try {
            String fromURL = optionalCrawlHeader.get().getCompareFromBaseUrl();
            String toURL = optionalCrawlHeader.get().getCompareToBaseUrl();
            Map<String, CrawlDetailEntity> fromEnv = new HashMap<>();
            Map<String, CrawlDetailEntity> toEnv = new HashMap<>();
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
            String fromPrefix = fromURLParts.length > 2 ? fromURLParts[0] : "";
            String[] toURLParts = toURLHost.split("\\.");
            String toPrefix = toURLParts.length > 2 ? toURLParts[0] : "";
            String fileName = fromPrefix + "_" + toPrefix + "_" + hostName + "_" + country + "_" + locale + "_" + dateTime;
            try (CSVWriter writer = new CSVWriter(new FileWriter(fileLocation + "/" + fileName + ".csv"))) {
                String[] headerBlock = {"URL " + hostName + "_" + country + "_" + locale,
                        "Parent Path "+fromPrefix,
                        "Status " + fromPrefix,
                        "Parent Path "+toPrefix,
                        "Status " + toPrefix,
                        "Count " + fromPrefix,
                        "Count " + toPrefix,
                        "Count Difference",
                        "Count Difference Percentage"};
                writer.writeNext(headerBlock);
                Set<String> uniquePath = new HashSet<>();
                for (CrawlDetailEntity detail : crawlDetails) {
                    String path = detail.getPath();
                    if (uniquePath.contains(path)) {
                        continue;
                    }
                    uniquePath.add(path);
                    String fromEnvStatus = fromEnv.containsKey(path) ? fromEnv.get(path).getProcessFlag() : LinkStatusEnum.MISSING.getValue();
                    String fromEnvParentPath = fromEnv.containsKey(path) ? fromEnv.get(path).getParentPath():"";
                    String toEnvStatus = toEnv.containsKey(path) ? toEnv.get(path).getProcessFlag() : LinkStatusEnum.MISSING.getValue();
                    String toEnvParentPAth =  toEnv.containsKey(path) ? toEnv.get(path).getParentPath() :"";
                    int countFromEnv = fromEnv.containsKey(path) ? (fromEnv.get(path).getProductCount() != null ? fromEnv.get(path).getProductCount() : 0) : 0;
                    int countToEnv = toEnv.containsKey(path) ? (toEnv.get(path).getProductCount() != null ? toEnv.get(path).getProductCount() : 0) : 0;
                    int countDifference = countFromEnv - countToEnv;
                    String countFrom = "";
                    String countTo = "";
                    String countDiff = "";
                    PageTypeEnum pageType = UrlUtil.getPageType(path);
                    if(pageType == PageTypeEnum.CATEGORY || pageType == PageTypeEnum.SEARCH)
                    {
                        if( fromEnvStatus.equals(LinkStatusEnum.SUCCESS.getValue()))
                            countFrom = String.valueOf(countFromEnv);
                        if( toEnvStatus.equals(LinkStatusEnum.SUCCESS.getValue()))
                            countTo = String.valueOf(countToEnv);
                        if( fromEnvStatus.equals(LinkStatusEnum.SUCCESS.getValue()) && toEnvStatus.equals(LinkStatusEnum.SUCCESS.getValue()))
                             countDiff= String.valueOf(countDifference);
                    }
                    String countPercentage = (countToEnv != 0 && !countDiff.isEmpty()) ? String.format("%.2f", 100.0 * countDifference / countToEnv) : "";
                    writeDetailsToCsv(path, fromEnvStatus, toEnvStatus, countFrom, countTo, countDiff, countPercentage, fromEnvParentPath,toEnvParentPAth,writer);
                }
            }
            updateStatus(reportId,ReportStatus.SUCCESS.getCode());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            updateStatus(reportId,ReportStatus.ERROR.getCode());
        }
    }
    private void  updateStatus(Long reportId, String message)
    {
        reportRepository.updateStatusAndTimeById(reportId, message);
    }
    private void writeDetailsToCsv(String path, String fromEnvStatus, String toEnvStatus,
                                   String countFromEnv, String countToEnv, String countDifference, String countPercentage,
                                   String fromParentPath, String toParentPath, CSVWriter writer) throws IOException {

        String[] record = {
                path,
                fromParentPath,
                fromEnvStatus,
                toParentPath,
                toEnvStatus,
                String.valueOf(countFromEnv),
                String.valueOf(countToEnv),
                String.valueOf(countDifference),
                countPercentage
        };
        writer.writeNext(record);

    }
}
