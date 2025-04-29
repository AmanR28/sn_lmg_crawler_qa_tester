package com.lmg.crawler_qa_tester.service;

import com.lmg.crawler_qa_tester.exception.PageAccessException;
import com.lmg.crawler_qa_tester.model.Domain;
import com.lmg.crawler_qa_tester.model.LinkEntity;
import com.lmg.crawler_qa_tester.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.lmg.crawler_qa_tester.util.Constants.PROD_CHANNEL;

@Service
@Slf4j
public class ProdConsumer {
    @Autowired
    private Domain prodDomain;
    @Autowired
    private Crawler crawler;
    @Autowired
    private LinkRepository linkRepository;
    @Autowired
    private DriverService driverService;

    @ServiceActivator(inputChannel = PROD_CHANNEL)
    public void crawlProd(Message<List<LinkEntity>> message) {

        processLinks(message, prodDomain);
    }

    private void processLinks(Message<List<LinkEntity>> message, Domain domain) {

        WebDriver prodDriver = driverService.getProdDriver();

        if (message.getPayload().isEmpty())
            return;

        LinkEntity link = message.getPayload().get(0);
        List<String> urls;

        log.info("Processing URL: {} for environment: {}", link.getUrl(), domain.getName());
        try {
            urls = crawler.crawl(prodDriver, link.getUrl());

            List<LinkEntity> newLinks = urls.stream()
                .map(url -> LinkEntity.builder().url(url).processed("N").type(domain.getName())
                    .build()).limit(10)
                .toList();
            link.setStatus("SUCCESS");
            link.setProcessed("Y");
            linkRepository.updateLink(link);
            log.info("Found {} links on page: {}", newLinks.size(), link.getUrl());
            linkRepository.saveLinks(newLinks);
        } catch (PageAccessException e) {
            String errorMessage = e.getStatusCode() != null ?
                String.format("HTTP %d: %s", e.getStatusCode(), e.getMessage()) :
                e.getMessage();
            link.setStatus("NOT_FOUND");
            link.setProcessed("Y");
            linkRepository.updateLink(link);
            log.error("Failed to access URL: {} - {}", e.getUrl(), errorMessage);
        } catch (Exception e) {
            log.error("Unexpected error crawling URL: {}", link.getUrl(), e);
        } finally {
            prodDriver.quit();
        }
    }

}
