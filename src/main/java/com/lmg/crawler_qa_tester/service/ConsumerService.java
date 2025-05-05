package com.lmg.crawler_qa_tester.service;

import com.lmg.crawler_qa_tester.constants.LinkStatus;
import com.lmg.crawler_qa_tester.dto.Link;
import com.lmg.crawler_qa_tester.mapper.CrawlDetailEntityMapper;
import com.lmg.crawler_qa_tester.repository.CrawlDetailRepository;
import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import com.lmg.crawler_qa_tester.util.BrowserFactory;
import com.microsoft.playwright.Browser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
    public void consumeProd(Message<Link> message) {

        Link link = message.getPayload();

        String linkUrl = link.getBaseUrl() + link.getPath();
        log.info("Processing Link: {}", linkUrl);

        Browser browser = BrowserFactory.getProdWebDriver(linkUrl);

        //TODO Page Status Validate

        List<String> urls = null;

        try {
            urls = pageService.processPage(browser, linkUrl);
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

    // TODO PRE PROD CONSUMER
    @ServiceActivator()
    public void consumePreProd() {

    }

}
