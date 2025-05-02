package com.lmg.crawler_qa_tester.service;

import com.lmg.crawler_qa_tester.constants.LinkStatus;
import com.lmg.crawler_qa_tester.dto.Link;
import com.lmg.crawler_qa_tester.mapper.CrawlDetailEntityMapper;
import com.lmg.crawler_qa_tester.repository.CrawlDetailRepository;
import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import com.lmg.crawler_qa_tester.util.WebDriverFactory;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ConsumerService {
    @Autowired
    private PageService pageService;
    @Autowired
    private CrawlDetailRepository crawlDetailRepository;

    @ServiceActivator(inputChannel = "prodChannel")
    public void consumeProd(Message<Link> message) {

        Link link = message.getPayload();

        String linkUrl = link.getBaseUrl() + link.getPath();
        log.info("Processing Link: {}", linkUrl);

        WebDriver driver = WebDriverFactory.getProdWebDriver(linkUrl);

        //TODO Page Status Validate

        List<String> urls = pageService.processPage(driver);

        List<CrawlDetailEntity> links =
            urls.stream().filter(url ->
                    (url.startsWith(link.getBaseUrl())
                        && !url.substring(link.getBaseUrl().length()).isEmpty()))
                .limit(10)
                .map(url -> new CrawlDetailEntityMapper().fromLink(Link.builder()
                    .crawlHeaderId(link.getCrawlHeaderId())
                    .env(link.getEnv())
                    .baseUrl(link.getBaseUrl())
                    .path(url.substring(link.getBaseUrl().length()))
                    .processFlag(LinkStatus.NOT_PROCESSED)
                    .build())).collect(Collectors.toList());
        log.info("Cur links size: {}", links.size());
            crawlDetailRepository.saveAll(links);

        driver.quit();
    }

    // TODO PRE PROD CONSUMER
    @ServiceActivator()
    public void consumePreProd() {

    }

}
