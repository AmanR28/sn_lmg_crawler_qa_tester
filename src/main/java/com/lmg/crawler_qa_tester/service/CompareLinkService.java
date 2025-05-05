package com.lmg.crawler_qa_tester.service;

import com.lmg.crawler_qa_tester.constants.EnvironmentEnum;
import com.lmg.crawler_qa_tester.constants.LinkStatus;
import com.lmg.crawler_qa_tester.repository.CrawlDetailRepository;
import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import com.lmg.crawler_qa_tester.util.WriteToCSV;
import com.opencsv.CSVWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CompareLinkService {
    @Autowired
    private CrawlDetailRepository crawlDetailRepository;
    public byte[]  getCompareReports(Integer id)
    {
      List<CrawlDetailEntity> crawlDetailEntityList =   crawlDetailRepository.findAllByCrawlHeaderId(id);
      List<CrawlDetailEntity> prodList = new ArrayList<>();
      List<CrawlDetailEntity> preProdList = new ArrayList<>();
        List<CrawlDetailEntity> prodListNotFound = new ArrayList<>();
        List<CrawlDetailEntity> preProdListNotFound = new ArrayList<>();
        List<CrawlDetailEntity> prodListSuccess = new ArrayList<>();
        List<CrawlDetailEntity> preProdListSuccess = new ArrayList<>();
        List<CrawlDetailEntity> prodListInProgress = new ArrayList<>();
        List<CrawlDetailEntity> preProdListInProgress = new ArrayList<>();
        List<CrawlDetailEntity> prodListFatal = new ArrayList<>();
        List<CrawlDetailEntity> preProdListFatal = new ArrayList<>();
        List<CrawlDetailEntity> prodListNotProceed = new ArrayList<>();
        List<CrawlDetailEntity> preProdListNotProceed = new ArrayList<>();

        if(crawlDetailEntityList.isEmpty())
      {
          throw new RuntimeException("No crawl details found for the provided id.");
      }
        for (CrawlDetailEntity detail : crawlDetailEntityList) {
            if (detail.getEnv().equals(EnvironmentEnum.PROD.getValue())) {
                prodList.add(detail);
                if(detail.getProcessFlag().equals(LinkStatus.NOT_PROCESSED.getValue()))
                {
                    prodListNotProceed.add(detail);
                }
                if(detail.getProcessFlag().equals(LinkStatus.SUCCESS.getValue()))
                {
                    prodListSuccess.add(detail);
                }
                if(detail.getProcessFlag().equals(LinkStatus.FATAL.getValue()))
                {
                    prodListFatal.add(detail);
                }
                if(detail.getProcessFlag().equals(LinkStatus.IN_PROGRESS.getValue()))
                {
                    prodListInProgress.add(detail);
                }
                if(detail.getProcessFlag().equals(LinkStatus.NOT_FOUND.getValue()))
                {
                    prodListNotFound.add(detail);
                }

            } else {
                preProdList.add(detail);
                if(detail.getProcessFlag().equals(LinkStatus.NOT_PROCESSED.getValue()))
                {
                    preProdListNotProceed.add(detail);
                }
                if(detail.getProcessFlag().equals(LinkStatus.SUCCESS.getValue()))
                {
                    preProdListSuccess.add(detail);
                }
                if(detail.getProcessFlag().equals(LinkStatus.FATAL.getValue()))
                {
                    preProdListFatal.add(detail);
                }
                if(detail.getProcessFlag().equals(LinkStatus.IN_PROGRESS.getValue()))
                {
                    preProdListInProgress.add(detail);
                }
                if(detail.getProcessFlag().equals(LinkStatus.NOT_FOUND.getValue()))
                {
                    preProdListNotFound.add(detail);
                }
            }
        }
        Set<CrawlDetailEntity> prodSet = new HashSet<>(prodList);
        Set<CrawlDetailEntity> preProdSet = new HashSet<>(preProdList);
        List<CrawlDetailEntity> prodNotInPreProd = getMissing(prodSet,preProdSet);

        List<CrawlDetailEntity> preProdNotInProd = getMissing(preProdSet,prodSet);
        try {


            WriteToCSV writeToCSV = new WriteToCSV();
            return writeToCSV.writeToCSV(prodListNotFound, preProdListNotFound, prodListSuccess, preProdListSuccess,
                    prodListInProgress, preProdListInProgress, prodListFatal, preProdListFatal,
                    prodListNotProceed, preProdListNotProceed,prodNotInPreProd,preProdNotInProd);



        } catch (IOException e) {
            throw new RuntimeException("Error generating CSV: " + e.getMessage(), e);
        }

    }
    private List<CrawlDetailEntity> getMissing(Set<CrawlDetailEntity> s1, Set<CrawlDetailEntity> s2) {
        s1.removeAll(s2);
        return new ArrayList<>(s1);
    }
}
