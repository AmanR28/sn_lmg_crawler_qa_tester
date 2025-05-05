package com.lmg.crawler_qa_tester.service;

import com.lmg.crawler_qa_tester.constants.ConsumerStatusEnum;
import com.lmg.crawler_qa_tester.constants.EnvironmentEnum;
import com.lmg.crawler_qa_tester.constants.LinkStatus;
import com.lmg.crawler_qa_tester.dto.Link;
import com.lmg.crawler_qa_tester.dto.Process;
import com.lmg.crawler_qa_tester.mapper.CrawlDetailEntityMapper;
import com.lmg.crawler_qa_tester.mapper.CrawlHeaderEntityMapper;
import com.lmg.crawler_qa_tester.repository.CrawlDetailRepository;
import com.lmg.crawler_qa_tester.repository.CrawlHeaderRepository;
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

  public Integer createProject(String prodBaseUrl, String preProdBaseUrl) {

    log.info(
        "Create project with prodBaseUrl: {} and preProdBaseUrl: {}", prodBaseUrl, preProdBaseUrl);

    Process process =
        Process.builder().prodBaseUrl(prodBaseUrl).preProdBaseUrl(preProdBaseUrl).build();
    Integer processId =
        crawlHeaderRepository.save(new CrawlHeaderEntityMapper().fromProcess(process)).getId();

    Link prodlink =
        Link.builder()
            .env(EnvironmentEnum.PROD)
            .baseUrl(prodBaseUrl)
            .crawlHeaderId(processId)
            .path("/")
            .processFlag(LinkStatus.NOT_PROCESSED)
            .build();
    crawlDetailRepository.save(new CrawlDetailEntityMapper().fromLink(prodlink));

    Link preProdlink =
        Link.builder()
            .env(EnvironmentEnum.PRE_PROD)
            .baseUrl(preProdBaseUrl)
            .crawlHeaderId(processId)
            .path("/")
            .processFlag(LinkStatus.NOT_PROCESSED)
            .build();
    crawlDetailRepository.save(new CrawlDetailEntityMapper().fromLink(preProdlink));

    log.info("Created project with id {}", processId);
    return processId;
  }

  public void startProject(Integer projectId) {

    log.info("Start project with id: {}", projectId);

    Process process =
        new CrawlHeaderEntityMapper().toProcess(crawlHeaderRepository.getReferenceById(projectId));
    process.setStatus(ConsumerStatusEnum.RUNNING);
    crawlHeaderRepository.save(new CrawlHeaderEntityMapper().fromProcess(process));

    AbstractPollingEndpoint endpoint =
        applicationContext.getBean(
            "messagePoller.inboundChannelAdapter", AbstractPollingEndpoint.class);
    endpoint.start();
    log.info("Started prod message poller");
  }

  public String getStatus() {

    return "OK";
  }
}
