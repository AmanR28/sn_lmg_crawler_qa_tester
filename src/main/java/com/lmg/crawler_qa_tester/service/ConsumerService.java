package com.lmg.crawler_qa_tester.service;

import com.lmg.crawler_qa_tester.repository.CrawlDetailRepository;
import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import com.lmg.crawler_qa_tester.util.WebDriverFactory;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ConsumerService {
    @Autowired
    private PageService pageService;
    @Autowired
    private CrawlDetailRepository crawlDetailRepository;

    @ServiceActivator(inputChannel = "prodChannel")
    public void consumeProd(Message<List<CrawlDetailEntity>> message) {

        CrawlDetailEntity link = message.getPayload().get(0);
        String linkUrl = link.getBaseUrl() + link.getPath();
        log.info("Processing Link: {}", linkUrl);

        WebDriver driver = WebDriverFactory.getProdWebDriver(linkUrl);

        //TODO Page Status Validate

        List<String> urls = pageService.processPage(driver);

        List<CrawlDetailEntity> links = urls.stream().map(url -> {
            CrawlDetailEntity crawlDetailEntity = new CrawlDetailEntity();
            crawlDetailEntity.setCrawlHeaderId(link.getCrawlHeaderId());
            crawlDetailEntity.setBaseUrl(link.getBaseUrl());
            crawlDetailEntity.setPath(url);
            return crawlDetailEntity;
        }).toList();
        log.info("Cur links size: {}", links.size());
        crawlDetailRepository.saveAll(links);

        driver.quit();
    }

    // TODO PRE PROD CONSUMER
    @ServiceActivator()
    public void consumePreProd() {

    }

}
