package com.lmg.crawler_qa_tester.service;

import com.lmg.crawler_qa_tester.constants.LinkStatusEnum;
import com.lmg.crawler_qa_tester.dto.Link;
import com.lmg.crawler_qa_tester.repository.CrawlRepository;
import com.lmg.crawler_qa_tester.util.BrowserFactory;
import com.lmg.crawler_qa_tester.util.UrlUtil;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConsumerService {

  @Autowired private PageService pageService;
  @Autowired private BrowserFactory browserFactory;
  @Autowired private CrawlRepository crawlRepository;

  @ServiceActivator(inputChannel = "linkConsumerChannel")
  public void consumeProd(Message<Link> message) {

    Link link = message.getPayload();
    LinkStatusEnum initialProcessFlag = link.getProcessFlag();
    log.info("Processing Prod Link: {}", link);

    try (Browser browser = browserFactory.getBrowser()) {
      Page page = browserFactory.getPage(browser, UrlUtil.getDomain(link.getBaseUrl()));

      List<String> urls = pageService.processPageData(page, link);

      if (initialProcessFlag.equals(LinkStatusEnum.PRE_MISSING)) {
        link.setProcessFlag(LinkStatusEnum.MISSING);
        crawlRepository.saveLink(link);
        return;
      }

      crawlRepository.saveLink(link);
      if (!link.getProcessFlag().equals(LinkStatusEnum.SUCCESS) || urls == null) return;

      List<Link> links =
          urls.stream()
              .map(
                  url ->
                      Link.builder()
                          .crawlHeaderId(link.getCrawlHeaderId())
                          .env(link.getEnv())
                          .baseUrl(link.getBaseUrl())
                          .depth(link.getDepth() + 1)
                          .path(url)
                          .parentPath(link.getPath())
                          .processFlag(LinkStatusEnum.NOT_PROCESSED)
                          .build())
              .toList();
      crawlRepository.saveNewLinks(links);
    } catch (Exception e) {
      link.setProcessFlag(LinkStatusEnum.FATAL);
      link.setErrorMessage("Browser Error : " + e.getMessage().substring(0, 255));
      crawlRepository.saveLink(link);
      log.error("Error processing link @ Browser : {} | {}", link, e);
    }
  }
}
