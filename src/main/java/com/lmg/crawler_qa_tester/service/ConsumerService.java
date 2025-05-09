package com.lmg.crawler_qa_tester.service;

import com.lmg.crawler_qa_tester.dto.Link;
import com.lmg.crawler_qa_tester.util.BrowserFactory;
import com.lmg.crawler_qa_tester.util.UrlUtil;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
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

  @ServiceActivator(inputChannel = "linkConsumerChannel")
  public void consumeProd(Message<Link> message) {

    Link link = message.getPayload();
    log.info("Processing Prod Link: {}", link);

    Browser browser = browserFactory.getBrowser();
    Page page = browserFactory.getPage(browser, UrlUtil.getDomain(link.getBaseUrl()));
    pageService.processPage(link, page);

    browser.close();
  }
}
