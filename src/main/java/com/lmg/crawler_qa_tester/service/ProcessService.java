package com.lmg.crawler_qa_tester.service;

import com.lmg.crawler_qa_tester.constants.ConsumerStatusEnum;
import com.lmg.crawler_qa_tester.constants.EnvironmentEnum;
import com.lmg.crawler_qa_tester.dto.Link;
import com.lmg.crawler_qa_tester.dto.Process;
import com.lmg.crawler_qa_tester.mapper.CrawlDetailEntityMapper;
import com.lmg.crawler_qa_tester.mapper.CrawlHeaderEntityMapper;
import com.lmg.crawler_qa_tester.repository.CrawlDetailRepository;
import com.lmg.crawler_qa_tester.repository.CrawlHeaderRepository;
import com.lmg.crawler_qa_tester.repository.entity.CrawlHeaderEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.endpoint.AbstractPollingEndpoint;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProcessService {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private CrawlHeaderRepository crawlHeaderRepository;
    @Autowired
    private CrawlDetailRepository crawlDetailRepository;

    public Integer createProject() {

        log.info("Create project");

        Process process = Process.builder().prodBaseUrl("https://www.centrepointstores.com/kw/en")
            .preProdBaseUrl("https://blc.centrepointstores.com/kw/en").build();
        Integer processId =
            crawlHeaderRepository.save(new CrawlHeaderEntityMapper().fromProcess(process)).getId();

        Link link = Link.builder().env(EnvironmentEnum.PROD.getValue())
            .baseUrl("https://www.centrepointstores.com/kw/en").crawlHeaderId(processId).path("/").build();
        crawlDetailRepository.save(new CrawlDetailEntityMapper().fromLink(link));

        log.info("Created project with id {}", processId);

        return processId;
    }

    public void startProject() {

        log.info("Start project");

        Process process =
            new CrawlHeaderEntityMapper().toProcess(crawlHeaderRepository.getReferenceById(1));
        process.setStatus(ConsumerStatusEnum.RUNNING);
        crawlHeaderRepository.save(new CrawlHeaderEntityMapper().fromProcess(process));

        AbstractPollingEndpoint endpoint =
            applicationContext.getBean("messagePoller.inboundChannelAdapter",
                AbstractPollingEndpoint.class);
        endpoint.start();
        log.info("Started prod message poller");
    }

    public String getStatus() {

        return "OK";
    }

}

