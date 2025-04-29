package com.lmg.crawler_qa_tester.service;

import com.lmg.crawler_qa_tester.exception.PageAccessException;
import com.lmg.crawler_qa_tester.model.Domain;
import com.lmg.crawler_qa_tester.model.LinkEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.lmg.crawler_qa_tester.util.Constants.PROD_CHANNEL;

@Service
@RequiredArgsConstructor
@Scope("prototype")
@Slf4j
public class ProdConsumer {
    @Qualifier("prodDomain")
    private final Domain prodDomain;
    @Qualifier("prodDriver")
    private final WebDriver prodDriver;

    private final Crawler crawler;

    @ServiceActivator(inputChannel = PROD_CHANNEL)
    public void crawlProd(Message<List<LinkEntity>> message) {

        processLinks(message, prodDomain);
    }

    private void processLinks(Message<List<LinkEntity>> message, Domain domain) {

        if (message.getPayload().isEmpty())
            return;

        LinkEntity link = message.getPayload().get(0);
        List<String> urls;

        log.info("Processing URL: {} for environment: {}", link.getUrl(), domain.getName());
        try {
            urls = crawler.crawl(prodDriver, link.getUrl());

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
