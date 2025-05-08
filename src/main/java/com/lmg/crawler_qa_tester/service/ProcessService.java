package com.lmg.crawler_qa_tester.service;

import com.lmg.crawler_qa_tester.constants.ProcessStatusEnum;
import com.lmg.crawler_qa_tester.constants.EnvironmentEnum;
import com.lmg.crawler_qa_tester.constants.LinkStatusEnum;
import com.lmg.crawler_qa_tester.dto.Link;
import com.lmg.crawler_qa_tester.dto.Process;
import com.lmg.crawler_qa_tester.mapper.CrawlDetailEntityMapper;
import com.lmg.crawler_qa_tester.mapper.CrawlHeaderEntityMapper;
import com.lmg.crawler_qa_tester.repository.CrawlDetailRepository;
import com.lmg.crawler_qa_tester.repository.CrawlHeaderRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.endpoint.AbstractPollingEndpoint;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProcessService {
  @Autowired private ApplicationContext applicationContext;
  @Autowired private CrawlHeaderRepository crawlHeaderRepository;
  @Autowired private CrawlDetailRepository crawlDetailRepository;

  @Transactional
  public Integer createProject(String prodBaseUrl, String preProdBaseUrl) {
    if (crawlHeaderRepository.hasActiveProcess())
      throw new RuntimeException("One Process is already running");
    if (prodBaseUrl.endsWith("/")) prodBaseUrl = prodBaseUrl.substring(0, prodBaseUrl.length() - 1);
    if (preProdBaseUrl.endsWith("/"))
      preProdBaseUrl = preProdBaseUrl.substring(0, preProdBaseUrl.length() - 1);
    log.info(
        "Create project with prodBaseUrl: {} and preProdBaseUrl: {}", prodBaseUrl, preProdBaseUrl);

    Process process =
        Process.builder()
            .prodBaseUrl(prodBaseUrl)
            .preProdBaseUrl(preProdBaseUrl)
            .status(ProcessStatusEnum.RUNNING)
            .build();
    Integer processId =
        crawlHeaderRepository.save(new CrawlHeaderEntityMapper().fromProcess(process)).getId();

    Link prodlink =
        Link.builder()
            .env(EnvironmentEnum.PROD)
            .baseUrl(prodBaseUrl)
            .crawlHeaderId(processId)
            .path("/")
            .processFlag(LinkStatusEnum.NOT_PROCESSED)
            .build();
    Link preProdlink =
        Link.builder()
            .env(EnvironmentEnum.PRE_PROD)
            .baseUrl(preProdBaseUrl)
            .crawlHeaderId(processId)
            .path("/")
            .processFlag(LinkStatusEnum.NOT_PROCESSED)
            .build();
    crawlDetailRepository.saveAll(
        List.of(
            new CrawlDetailEntityMapper().fromLink(prodlink),
            new CrawlDetailEntityMapper().fromLink(preProdlink)));
    log.info("Created project with id {}", processId);

    AbstractPollingEndpoint endpoint =
        applicationContext.getBean(
            "messagePoller.inboundChannelAdapter", AbstractPollingEndpoint.class);
    endpoint.start();
    log.info("Started prod message poller");

    return processId;
  }
}
