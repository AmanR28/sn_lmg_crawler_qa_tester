package com.lmg.crawler_qa_tester.service;

import com.lmg.crawler_qa_tester.constants.EnvironmentEnum;
import com.lmg.crawler_qa_tester.constants.LinkStatusEnum;
import com.lmg.crawler_qa_tester.constants.ProcessStatusEnum;
import com.lmg.crawler_qa_tester.dto.Link;
import com.lmg.crawler_qa_tester.dto.Process;
import com.lmg.crawler_qa_tester.repository.CrawlRepository;
import com.lmg.crawler_qa_tester.util.UrlUtil;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProcessService {
  @Autowired private ApplicationContext applicationContext;
  @Autowired private CrawlRepository crawlRepository;

  @Value("${env.app.consumerThread}")
  private int CONSUMER_THREAD;

  @Transactional
  public Integer createProject(String compareFromBaseUrl, String compareToBaseUrl) {
    if (crawlRepository.hasActiveProcess())
      throw new RuntimeException("One Process is already running");

    if (compareFromBaseUrl.endsWith("/"))
      compareFromBaseUrl = compareFromBaseUrl.substring(0, compareFromBaseUrl.length() - 1);

    if (compareToBaseUrl != null) {
      if (compareToBaseUrl.endsWith("/"))
        compareToBaseUrl = compareToBaseUrl.substring(0, compareToBaseUrl.length() - 1);
      if (!UrlUtil.getDomain(compareFromBaseUrl).equals(UrlUtil.getDomain(compareToBaseUrl)))
        throw new RuntimeException("CompareFrom Domain doesn't match CompareTo Domain");
      if (!UrlUtil.getCountry(compareFromBaseUrl).equals(UrlUtil.getCountry(compareToBaseUrl)))
        throw new RuntimeException("CompareFrom Country doesn't match CompareTo Country");
      if (!UrlUtil.getLocale(compareFromBaseUrl).equals(UrlUtil.getLocale(compareToBaseUrl)))
        throw new RuntimeException("CompareFrom Locale doesn't match CompareTo Locale");
    }

    log.info(
        "Create project with compareFromBaseUrl: {} and compareToBaseUrl: {}",
        compareFromBaseUrl,
        compareToBaseUrl);

    String finalCompareFromBaseUrl = compareFromBaseUrl;
    String finalCompareToBaseUrl = compareToBaseUrl;
    Process process =
        Process.builder()
            .compareFromBaseUrl(finalCompareFromBaseUrl)
            .compareToBaseUrl(finalCompareToBaseUrl)
            .status(ProcessStatusEnum.NEW)
            .domain(UrlUtil.getDomain(finalCompareFromBaseUrl))
            .country(UrlUtil.getCountry(finalCompareFromBaseUrl))
            .locale(UrlUtil.getLocale(finalCompareFromBaseUrl))
            .department("men")
            .consumerThread(CONSUMER_THREAD)
            .build();
    Process p1 = crawlRepository.saveProcess(process);
    log.info("Created project");

    return p1.getId();
  }

  @Transactional
  @ServiceActivator(inputChannel = "newProcessConsumerChannel")
  public void consumeNewProcess(Message<List<Process>> message) {
    Process process = message.getPayload().get(0);
    Link fromlink =
        Link.builder()
            .crawlHeaderId(process.getId())
            .env(EnvironmentEnum.FROM_ENV)
            .baseUrl(process.getCompareFromBaseUrl())
            .path("/")
            .processFlag(LinkStatusEnum.NOT_PROCESSED)
            .depth(0)
            .build();
    crawlRepository.saveLink(fromlink);

    if (process.getCompareToBaseUrl() != null) {
      Link tolink =
          Link.builder()
              .crawlHeaderId(process.getId())
              .env(EnvironmentEnum.TO_ENV)
              .baseUrl(process.getCompareToBaseUrl())
              .path("/")
              .processFlag(LinkStatusEnum.NOT_PROCESSED)
              .depth(0)
              .build();
      crawlRepository.saveLink(tolink);
    }

    process.setStatus(ProcessStatusEnum.RUNNING);
    crawlRepository.saveProcess(process);
  }

  @Transactional
  @ServiceActivator(inputChannel = "statusProcessPollerChannel")
  public void consumeCompleteProcess(Message<Integer> message) {
    Integer count = message.getPayload();
    if (count != 0) return;

    Process runningProcess = crawlRepository.getProcessByStatus(ProcessStatusEnum.RUNNING);
    Process postRunningProcess = crawlRepository.getProcessByStatus(ProcessStatusEnum.POST_RUNNING);

    if (runningProcess != null) {
      if (runningProcess.getCompareToBaseUrl() == null) {
        setProcessCompleted(runningProcess);
      } else {
        setProcessPostRunning(runningProcess);
      }
    }
    if (postRunningProcess != null) {
      setProcessCompleted(postRunningProcess);
    }
  }

  private void setProcessPostRunning(Process process) {
    startPostRunningProcess(process.getId());
    process.setStatus(ProcessStatusEnum.POST_RUNNING);
    process.setPageCount(crawlRepository.getLinkCountByProcessId(process.getId()));
    crawlRepository.saveProcess(process);
  }

  private void setProcessCompleted(Process process) {

    process.setStatus(ProcessStatusEnum.COMPLETED);
    process.setPageCount(crawlRepository.getLinkCountByProcessId(process.getId()));
    crawlRepository.saveProcess(process);
  }

  private void startPostRunningProcess(Integer processId) {
    List<Link> missingLinks = new ArrayList<>();
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
                .filter(e -> e.getEnv().equals(EnvironmentEnum.FROM_ENV))
                .collect(Collectors.toMap(Link::getPath, e -> e)));
    HashMap<String, Link> preProdMap =
        new HashMap<>(
            links.stream()
                .filter(e -> e.getEnv().equals(EnvironmentEnum.TO_ENV))
                .collect(Collectors.toMap(Link::getPath, e -> e)));

    for (String path : uniquePaths) {
      if (!prodMap.containsKey(path)) {
        Link preProdLink = preProdMap.get(path);
        if (preProdLink != null) {
          preProdLink.setProcessFlag(LinkStatusEnum.PRE_MISSING);
          crawlRepository.saveLink(preProdLink);
          missingLinks.add(preProdLink);
        }
      }
      if (!preProdMap.containsKey(path)) {
        Link prodLink = prodMap.get(path);
        prodLink.setProcessFlag(LinkStatusEnum.PRE_MISSING);
        crawlRepository.saveLink(prodLink);
        missingLinks.add(prodLink);
      }
    }

    crawlRepository.saveNewLinks(missingLinks);
  }
}
