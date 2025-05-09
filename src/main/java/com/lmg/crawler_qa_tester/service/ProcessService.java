package com.lmg.crawler_qa_tester.service;

import com.lmg.crawler_qa_tester.constants.EnvironmentEnum;
import com.lmg.crawler_qa_tester.constants.LinkStatusEnum;
import com.lmg.crawler_qa_tester.constants.ProcessStatusEnum;
import com.lmg.crawler_qa_tester.dto.Link;
import com.lmg.crawler_qa_tester.dto.Process;
import com.lmg.crawler_qa_tester.repository.CrawlRepository;
import com.lmg.crawler_qa_tester.repository.DomainRepository;
import com.lmg.crawler_qa_tester.util.UrlUtil;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.endpoint.AbstractPollingEndpoint;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProcessService {
  @Autowired private ApplicationContext applicationContext;
  @Autowired private CrawlRepository crawlRepository;
  @Autowired private DomainRepository domainRepository;

  @Value("${env.app.consumerThread}")
  private int CONSUMER_THREAD;

  @Transactional
  public void createProject(String compareFromBaseUrl, String compareToBaseUrl) {
    if (crawlRepository.hasActiveProcess())
      throw new RuntimeException("One Process is already running");

    if (compareFromBaseUrl.endsWith("/"))
      compareFromBaseUrl = compareFromBaseUrl.substring(0, compareFromBaseUrl.length() - 1);
    if (compareToBaseUrl.endsWith("/"))
      compareToBaseUrl = compareToBaseUrl.substring(0, compareToBaseUrl.length() - 1);

    if (!UrlUtil.getDomain(compareFromBaseUrl).equals(UrlUtil.getDomain(compareToBaseUrl)))
      throw new RuntimeException("CompareFrom Domain doesn't match CompareTo Domain");
    if (!UrlUtil.getCountry(compareFromBaseUrl).equals(UrlUtil.getCountry(compareToBaseUrl)))
      throw new RuntimeException("CompareFrom Country doesn't match CompareTo Country");
    if (!UrlUtil.getLocale(compareToBaseUrl).equals(UrlUtil.getLocale(compareFromBaseUrl)))
      throw new RuntimeException("CompareFrom Locale doesn't match CompareTo Locale");

    log.info(
        "Create project with compareFromBaseUrl: {} and compareToBaseUrl: {}",
        compareFromBaseUrl,
        compareToBaseUrl);

    List<String> departments =
        domainRepository.getDepartments(UrlUtil.getDomain(compareFromBaseUrl));

    String finalCompareFromBaseUrl = compareFromBaseUrl;
    String finalCompareToBaseUrl = compareToBaseUrl;
    List<Process> processes =
        departments.stream()
            .map(
                department ->
                    Process.builder()
                        .compareFromBaseUrl(finalCompareFromBaseUrl)
                        .compareToBaseUrl(finalCompareToBaseUrl)
                        .status(ProcessStatusEnum.NEW)
                        .domain(UrlUtil.getDomain(finalCompareFromBaseUrl))
                        .country(UrlUtil.getCountry(finalCompareFromBaseUrl))
                        .locale(UrlUtil.getLocale(finalCompareFromBaseUrl))
                        .department(department)
                        .consumerThread(CONSUMER_THREAD)
                        .build())
            .toList();
    crawlRepository.saveAllProcesses(processes);
    log.info("Created project");

    AbstractPollingEndpoint endpoint =
        applicationContext.getBean(
            "messagePoller.inboundChannelAdapter", AbstractPollingEndpoint.class);
    endpoint.start();
    log.info("Started prod message poller");
  }

  @Transactional
  @ServiceActivator(inputChannel = "processConsumerChannel")
  public void consumeNewProcess(Message<Process> message) {
    Process process = message.getPayload();
    Link fromlink =
        Link.builder()
            .crawlHeaderId(process.getId())
            .env(EnvironmentEnum.FROM_ENV)
            .baseUrl(process.getCompareFromBaseUrl())
            .path("/")
            .processFlag(LinkStatusEnum.NOT_PROCESSED)
            .depth(0)
            .build();
    Link tolink =
        Link.builder()
            .crawlHeaderId(process.getId())
            .env(EnvironmentEnum.TO_ENV)
            .baseUrl(process.getCompareToBaseUrl())
            .path("/")
            .processFlag(LinkStatusEnum.NOT_PROCESSED)
            .depth(0)
            .build();
    process.setStatus(ProcessStatusEnum.RUNNING);
    crawlRepository.saveNewLinks(List.of(fromlink, tolink));
    crawlRepository.saveProcess(process);
  }
}
