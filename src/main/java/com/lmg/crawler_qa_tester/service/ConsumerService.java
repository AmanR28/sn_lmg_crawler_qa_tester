package com.lmg.crawler_qa_tester.service;

import com.lmg.crawler_qa_tester.constants.LinkStatus;
import com.lmg.crawler_qa_tester.dto.Link;
import com.lmg.crawler_qa_tester.mapper.CrawlDetailEntityMapper;
import com.lmg.crawler_qa_tester.repository.CrawlDetailRepository;
import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import com.lmg.crawler_qa_tester.util.BrowserFactory;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        log.info("Processing Prod Link: {}", link);

        Browser browser = BrowserFactory.getProdWebDriver();

        processPage(link, browser);
    }

    @ServiceActivator(inputChannel = "preProdChannel")
    public void consumePreProd(Message<Link> message) {

        Link link = message.getPayload();
        log.info("Processing PreProd Link: {}", link);

        Browser browser = BrowserFactory.getPreProdWebDriver();

        processPage(link, browser);
    }

    private void processPage(Link link, Browser browser) {
        String linkUrl = link.getBaseUrl() + link.getPath();

        Page page = BrowserFactory.getBrowserContext(browser).newPage();
        page.navigate(linkUrl);
        page.waitForTimeout(3000);

        boolean isValidPage = pageService.validatePageStatus(page);
        if (!isValidPage) {
            link.setProcessFlag(LinkStatus.NOT_FOUND);
            crawlDetailRepository.save(new CrawlDetailEntityMapper().fromLink(link));
            browser.close();
            return;
        }

        List<String> urls = null;

        try {
            urls = pageService.processPage(page);
            link.setProcessFlag(LinkStatus.SUCCESS);
            crawlDetailRepository.save(new CrawlDetailEntityMapper().fromLink(link));
        } catch (Exception e) {
            log.error("Error processing page", e);
            link.setProcessFlag(LinkStatus.FATAL);
            crawlDetailRepository.save(new CrawlDetailEntityMapper().fromLink(link));
            return;
        }

        List<CrawlDetailEntity> links =
                urls.stream().filter(url -> (url.startsWith("/kw/en/")))
                        .limit(5)
                        .map(url -> new CrawlDetailEntityMapper().fromLink(Link.builder()
                                .crawlHeaderId(link.getCrawlHeaderId())
                                .env(link.getEnv())
                                .baseUrl(link.getBaseUrl())
                                .path(url)
                                .processFlag(LinkStatus.NOT_PROCESSED)
                                .build())).toList();
        log.info("Cur links size: {}", links.size());

        try {
            crawlDetailRepository.saveAll(links);
        } catch (DataIntegrityViolationException ignored) {
        } catch (Exception e) {
            log.error("Error saving links", e);
        }

        browser.close();
    }
}
