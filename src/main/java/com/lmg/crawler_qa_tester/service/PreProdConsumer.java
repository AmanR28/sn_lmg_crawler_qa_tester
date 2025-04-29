package com.lmg.crawler_qa_tester.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import com.lmg.crawler_qa_tester.exception.PageAccessException;
import com.lmg.crawler_qa_tester.model.Domain;
import com.lmg.crawler_qa_tester.model.LinkEntity;

import java.util.List;

import static com.lmg.crawler_qa_tester.util.Constants.PRE_PROD_CHANNEL;

@Service
@RequiredArgsConstructor
@Slf4j
public class PreProdConsumer {
    @Qualifier("preProdDomain")
    private final Domain preProdDomain;
    @Qualifier("preProdDriver")
    private final WebDriver preProdDriver;

    private final Crawler crawler;

    @ServiceActivator(inputChannel = PRE_PROD_CHANNEL)
    public void crawlPreProd(Message<List<LinkEntity>> message) {

        processLinks(message, preProdDomain);
    }

    private void processLinks(Message<List<LinkEntity>> message, Domain domain) {

        if (message.getPayload().isEmpty())
            return;

        LinkEntity link = message.getPayload().get(0);
        List<String> urls;

        log.info("Processing URL: {} for environment: {}", link.getUrl(), domain.getName());
        try {
            urls = crawler.crawl(preProdDriver, link.getUrl());

            List<LinkEntity> newLinks = urls.stream()
                .map(url -> LinkEntity.builder().url(url).processed("N").type(domain.getName())
                    .build())
                .toList();
            link.setStatus("SUCCESS");
            log.info("Found {} links on page: {}", newLinks.size(), link.getUrl());
        } catch (PageAccessException e) {
            String errorMessage = e.getStatusCode() != null ?
                String.format("HTTP %d: %s", e.getStatusCode(), e.getMessage()) :
                e.getMessage();
            link.setStatus("NOT_FOUND");
            log.error("Failed to access URL: {} - {}", e.getUrl(), errorMessage);
        } catch (Exception e) {
            log.error("Unexpected error crawling URL: {}", link.getUrl(), e);
        }
    }

}
