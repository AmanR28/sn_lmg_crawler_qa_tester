package com.lmg.crawler_qa_tester.service;

import com.lmg.crawler_qa_tester.repository.LinkRepository;
import com.lmg.crawler_qa_tester.repository.entity.LinkEntity;
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
    private LinkRepository linkRepository;

    @ServiceActivator()
    public void consumeProd(Message<List<LinkEntity>> message) {

        LinkEntity link = message.getPayload().get(0);
        String linkUrl = link.getBaseUrl() + link.getUrl();
        log.info("Processing Link: {}", linkUrl);

        WebDriver driver = WebDriverFactory.getProdWebDriver(linkUrl);

        //TODO Page Status Validate

        List<String> urls = pageService.processPage(driver);

        List<LinkEntity> links = urls.stream().map(url -> {
            LinkEntity linkEntity = new LinkEntity();
            linkEntity.setUrl(url);
            return linkEntity;
        }).toList();
        log.info("Cur links size: {}", links.size());
//        linkRepository.saveAll(links);

        driver.quit();
    }

    // TODO PRE PROD CONSUMER
    @ServiceActivator()
    public void consumePreProd() {

    }

}
