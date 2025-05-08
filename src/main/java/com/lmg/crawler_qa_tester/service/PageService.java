package com.lmg.crawler_qa_tester.service;

import com.lmg.crawler_qa_tester.constants.LinkStatusEnum;
import com.lmg.crawler_qa_tester.dto.Link;
import com.lmg.crawler_qa_tester.repository.mapper.CrawlDetailEntityMapper;
import com.lmg.crawler_qa_tester.repository.internal.CrawlDetailRepository;
import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import com.microsoft.playwright.*;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.uri.Uri;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PageService {
  @Autowired private CrawlDetailRepository crawlDetailRepository;

  public void processPage(Link link, Page page) {
    Response response = page.navigate(link.getBaseUrl() + link.getPath());
    page.waitForTimeout(3000);

    try {
      validatePage(response, page, link);
    } catch (RuntimeException e) {
      log.error("ProcessPage Error Link : {} ", link);
      log.error("Error processing page", e);
      return;
    }

    List<String> urls = null;

    try {
      urls = getPageUrls(page);
      link.setProcessFlag(LinkStatusEnum.SUCCESS);
      crawlDetailRepository.save(new CrawlDetailEntityMapper().fromLink(link));
    } catch (Exception e) {
      log.error("Error processing page", e);
      link.setProcessFlag(LinkStatusEnum.FATAL);
      crawlDetailRepository.save(new CrawlDetailEntityMapper().fromLink(link));
      return;
    }

    String startPath = Uri.create(link.getBaseUrl()).getPath();

    List<CrawlDetailEntity> links =
        urls.stream()
            .filter(
                url -> (url.startsWith(startPath) && !url.substring(startPath.length()).isEmpty()))
            .map(
                url ->
                    new CrawlDetailEntityMapper()
                        .fromLink(
                            Link.builder()
                                .crawlHeaderId(link.getCrawlHeaderId())
                                .env(link.getEnv())
                                .baseUrl(link.getBaseUrl())
                                    .depth(link.getDepth() + 1)
                                .path(url.substring(startPath.length()))
                                .processFlag(LinkStatusEnum.NOT_PROCESSED)
                                .build()))
            .toList();
    log.info("Cur links size: {}", links.size());
    crawlDetailRepository.saveNewLinks(links);
  }

  List<String> getPageUrls(Page page) {

    List<ElementHandle> linkElements = page.querySelectorAll("a");

    List<String> pageLinks = new ArrayList<>();
    for (ElementHandle link : linkElements) {
      String href = link.getAttribute("href");
      if (href != null && !href.trim().isEmpty()) {
        pageLinks.add(href);
      }
    }
    return pageLinks;
  }

  void validatePage(Response response, Page page, Link link) {
    int status = response.status();
    if (status >= 200 && status < 300) {
      link.setProcessFlag(LinkStatusEnum.SUCCESS);
    } else if (status >= 400 && status < 500) {
      link.setProcessFlag(LinkStatusEnum.NOT_FOUND);
    } else if (status >= 500 && status < 600) {
      link.setProcessFlag(LinkStatusEnum.FATAL);
    }
    crawlDetailRepository.save(new CrawlDetailEntityMapper().fromLink(link));
    if (link.getProcessFlag() != LinkStatusEnum.SUCCESS) {
      throw new RuntimeException("Error processing page");
    }
  }
}
