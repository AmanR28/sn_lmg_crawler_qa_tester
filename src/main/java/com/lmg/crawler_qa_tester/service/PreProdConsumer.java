package com.lmg.crawler_qa_tester.service;

import com.lmg.crawler_qa_tester.exception.PageAccessException;
import com.lmg.crawler_qa_tester.model.Domain;
import com.lmg.crawler_qa_tester.model.LinkEntity;
import com.lmg.crawler_qa_tester.repository.LinkRepository;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.lmg.crawler_qa_tester.util.Constants.PRE_PROD_CHANNEL;

@Service
@Slf4j
public class PreProdConsumer {
    @Autowired
    private Domain preProdDomain;
    @Autowired
    private Crawler crawler;
    @Autowired
    private LinkRepository linkRepository;
    @Autowired
    private DriverService driverService;

    @ServiceActivator(inputChannel = PRE_PROD_CHANNEL)
    public void crawlPreProd(Message<List<LinkEntity>> message) {

        processLinks(message, preProdDomain);
    }

    private void processLinks(Message<List<LinkEntity>> message, Domain domain) {

        WebDriver preProdDriver = driverService.getPreProdDriver();

        if (message.getPayload().isEmpty())
            return;

        LinkEntity link = message.getPayload().get(0);
        List<String> urls;

        log.info("Processing URL: {} for environment: {}", link.getUrl(), domain.getName());
        try {
            urls = crawler.crawl(preProdDriver, link.getUrl());

            List<LinkEntity> newLinks = urls.stream()
                .map(url -> LinkEntity.builder().url(url).processed("N").type(domain.getName())
                    .build()).limit(3)
                .toList();
            link.setStatus("SUCCESS");
            log.info("Found {} links on page: {}", newLinks.size(), link.getUrl());
            linkRepository.saveLinks(newLinks);
        } catch (PageAccessException e) {
            String errorMessage = e.getStatusCode() != null ?
                String.format("HTTP %d: %s", e.getStatusCode(), e.getMessage()) :
                e.getMessage();
            link.setStatus("NOT_FOUND");
            log.error("Failed to access URL: {} - {}", e.getUrl(), errorMessage);
        } catch (Exception e) {
            log.error("Unexpected error crawling URL: {}", link.getUrl(), e);
        } finally {
            preProdDriver.quit();
        }
    }

}
