package com.lmg.crawler_qa_tester.service;

import com.lmg.crawler_qa_tester.dto.Link;
import com.lmg.crawler_qa_tester.util.BrowserFactory;
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

  @ServiceActivator(inputChannel = "prodChannel")
  public void consumeProd(Message<Link> message) {

    Link link = message.getPayload();
    log.info("Processing Prod Link: {}", link);

    Browser browser = browserFactory.getProdWebDriver();
    Page page = browserFactory.getProdBrowserPage(browser);
    pageService.processPage(link, page);

    browser.close();
  }

  @ServiceActivator(inputChannel = "preProdChannel")
  public void consumePreProd(Message<Link> message) {

    Link link = message.getPayload();
    log.info("Processing PreProd Link: {}", link);

    Browser browser = browserFactory.getPreProdWebDriver();
    Page page = browserFactory.getPreProdBrowserPage(browser);
    pageService.processPage(link, page);

    browser.close();
  }
}
