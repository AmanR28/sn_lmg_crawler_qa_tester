package com.lmg.crawler_qa_tester.service;

import com.lmg.crawler_qa_tester.model.Domain;
import com.lmg.crawler_qa_tester.model.LinkEntity;
import com.lmg.crawler_qa_tester.repository.LinkRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SchedulerService {
    @Autowired
    private Domain prodDomain;
    @Autowired
    private Domain preProdDomain;
    @Autowired
    private LinkRepository linkRepository;

    @Scheduled(fixedRate = 1000 * 60 * 10, initialDelay = 500)
    public void initiateTable() {

        linkRepository.regenerateTable();
    }

    @Scheduled(fixedRate = 1000 * 60 * 10, initialDelay = 2000)
    public void initiateProd() {

        LinkEntity link = LinkEntity.builder().url(prodDomain.getBaseUrl()).processed("N")
            .type(prodDomain.getName()).build();
        linkRepository.saveLink(link);

        log.info("Initiated Prod Domain: " + prodDomain.getName());
    }

    @Scheduled(fixedRate = 1000 * 60 * 10, initialDelay = 2000)
    public void initiatePreProd() {

        linkRepository.regenerateTable();
        LinkEntity link = LinkEntity.builder().url(preProdDomain.getBaseUrl()).processed("N")
            .type(preProdDomain.getName()).build();
        linkRepository.saveLink(link);

        log.info("Initiated PreProd Domain: " + preProdDomain.getName());
    }

//    @Scheduled(fixedRate = 1000, initialDelay = 5000)
    public void pollerProd() {

        LinkEntity link = linkRepository.getAnUnprocessedLink(prodDomain.getName());
        if (link != null) {
            link.setProcessed("Y");
            linkRepository.updateLink(link);
            log.debug("Polled Prod Domain: " + link.getUrl());
        }
    }

    @Scheduled(fixedRate = 1000, initialDelay = 5000)
    public void pollerPreProd() {

        LinkEntity link = linkRepository.getAnUnprocessedLink(preProdDomain.getName());
        if (link != null) {
            link.setProcessed("Y");
            linkRepository.updateLink(link);
            log.debug("Polled PreProd Domain: " + link.getUrl());
        }
    }

}
