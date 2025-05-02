package com.lmg.crawler_qa_tester.service;

import com.lmg.crawler_qa_tester.config.AppConfig;
import com.lmg.crawler_qa_tester.constants.ConsumerStatusEnum;
import com.lmg.crawler_qa_tester.dto.Domain;
import com.lmg.crawler_qa_tester.repository.LinkRepository;
import com.lmg.crawler_qa_tester.repository.ProcessRepository;
import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
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
    private AppConfig appConfig;
    @Autowired
    private ProcessRepository processRepository;
    @Autowired
    private LinkRepository linkRepository;
    @Autowired
    private Domain prodDomain;

    private Integer id = null;

    public Integer createProject() {

        log.info("Create project");

        CrawlHeaderEntity crawlHeaderEntity = new CrawlHeaderEntity();
        crawlHeaderEntity.setProdBaseUrl("https://www.centrepointstores.com/kw/en");
        crawlHeaderEntity.setPreProdBaseUrl("https://blc.centrepointstores.com/kw/en");
        crawlHeaderEntity.setStatus(ConsumerStatusEnum.INIT);
        CrawlHeaderEntity savedEntity = processRepository.save(crawlHeaderEntity);

        prodDomain.setBaseUrl("https://www.centrepointstores.com/kw/en");

        CrawlDetailEntity prodLink = new CrawlDetailEntity();
        prodLink.setCrawlHeaderId(savedEntity.getId());
        prodLink.setBaseUrl(savedEntity.getProdBaseUrl());
        prodLink.setPath("/");
        linkRepository.save(prodLink);

        this.id = savedEntity.getId();

        return savedEntity.getId();
    }

    public void startProject() {

        log.info("Start project");

        if (appConfig.getIsRunning()) {
            throw new RuntimeException("Project is already running");
        }
        appConfig.setIsRunning(true);
        appConfig.setRunningProjectId(this.id);

        CrawlHeaderEntity project = processRepository.getReferenceById(1);
        prodDomain.setBaseUrl(project.getProdBaseUrl());

        project.setStatus(ConsumerStatusEnum.RUNNING);
        processRepository.save(project);

        AbstractPollingEndpoint endpoint =
            applicationContext.getBean("prodMessagePoller.inboundChannelAdapter",
                AbstractPollingEndpoint.class);
        endpoint.start();
        log.info("Started prod message poller");
    }

    public void stopProject() {

        log.info("Stop project");

        try {
            AbstractPollingEndpoint endpoint =
                applicationContext.getBean("prodMessagePoller.inboundChannelAdapter",
                    AbstractPollingEndpoint.class);
            if (endpoint.isRunning()) {
                endpoint.stop();
                log.info("Stopped prod message poller");
            }
        } catch (Exception e) {
            log.error("Error stopping prod message poller", e);
        }

        if (appConfig.getRunningProjectId() != null) {
            CrawlHeaderEntity project =
                processRepository.getReferenceById(appConfig.getRunningProjectId());
            project.setStatus(ConsumerStatusEnum.STOPPED);
            processRepository.save(project);
        }

        appConfig.setIsRunning(false);
        appConfig.setRunningProjectId(null);
    }

    public void cancelProject() {

        log.info("Cancel project");

        try {
            AbstractPollingEndpoint endpoint =
                applicationContext.getBean("prodMessagePoller.inboundChannelAdapter",
                    AbstractPollingEndpoint.class);
            if (endpoint.isRunning()) {
                endpoint.stop();
                log.info("Stopped prod message poller");
            }
        } catch (Exception e) {
            log.error("Error stopping prod message poller", e);
        }

        if (appConfig.getRunningProjectId() != null) {
            CrawlHeaderEntity project =
                processRepository.getReferenceById(appConfig.getRunningProjectId());
            project.setStatus(ConsumerStatusEnum.CANCELLED);
            processRepository.save(project);
        }

        appConfig.setIsRunning(false);
        appConfig.setRunningProjectId(null);
    }

}

