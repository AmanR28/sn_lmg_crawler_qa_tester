package com.lmg.crawler_qa_tester.service;

import com.lmg.crawler_qa_tester.constants.LinkStatusEnum;
import com.lmg.crawler_qa_tester.constants.PageTypeEnum;
import com.lmg.crawler_qa_tester.dto.Link;
import com.lmg.crawler_qa_tester.util.UrlUtil;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PageService {
  @Value("${env.app.pageWait}")
  int PAGE_WAIT;

  public List<String> processPageData(Page page, Link link) {
    PageTypeEnum pageType = UrlUtil.getPageType(link.getPath());

    Response response = page.navigate(link.getBaseUrl() + link.getPath());
    page.waitForTimeout(PAGE_WAIT);
    String pageText = page.innerText("body");
    List<String> urls = extractPageUrls(page);

    getPageStatus(link, page, response);
    if (!link.getProcessFlag().equals(LinkStatusEnum.SUCCESS)) return null;

    getErrorPageStatus(link, page, pageText);
    if (!link.getProcessFlag().equals(LinkStatusEnum.SUCCESS)) return null;

    if (pageType == PageTypeEnum.CATEGORY) getCategoryPageStatus(link, pageText);
    if (pageType == PageTypeEnum.SEARCH) getSearchPageStatus(link, pageText);
    if (!link.getProcessFlag().equals(LinkStatusEnum.SUCCESS)) return null;

    if (pageType == PageTypeEnum.CATEGORY) return null;
    if (pageType == PageTypeEnum.SEARCH) return null;
    return getPageUrls(link, urls);
  }


  private void getPageStatus(Link link, Page page, Response response) {
    int status = response.status();
    if (status >= 200 && status < 300) {
      link.setProcessFlag(LinkStatusEnum.SUCCESS);
    } else if (status >= 400 && status < 500) {
      link.setProcessFlag(LinkStatusEnum.NOT_FOUND);
      link.setErrorMessage("Status Error : " + status);
      log.error("Error link @ Status : {} | {}", link, status);
    } else if (status >= 500 && status < 600) {
      link.setProcessFlag(LinkStatusEnum.FATAL);
      link.setErrorMessage("Status Error : " + status);
      log.error("Error processing link @ Status : {} | {}", link, status);
    }
  }

  private void getErrorPageStatus(Link link, Page page, String pageText) {
    if (page.url().contains("404") || pageText.contains("HMM, THIS ISN'T RIGHT")) {
      link.setProcessFlag(LinkStatusEnum.NOT_FOUND);
      link.setErrorMessage("Server Error : " + page.url());
      log.error("Error processing link @ Server : {} | {}", link, page.url());
    } else {
      link.setProcessFlag(LinkStatusEnum.SUCCESS);
    }
  }

  private void getCategoryPageStatus(Link link, String pageText) {
    String regex = "[0-9]+ Product[s]*";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(pageText);

    if (matcher.find()) {
      String countStr = matcher.group(0).split(" ")[0];
      int productCount = Integer.parseInt(countStr);
      link.setProductCount(productCount);
      if (productCount > 0) link.setProcessFlag(LinkStatusEnum.SUCCESS);
      else link.setProcessFlag(LinkStatusEnum.INVALID_COUNT);
    } else {
      link.setProcessFlag(LinkStatusEnum.FATAL);
      link.setErrorMessage("Page Error : Failed to Find Product Count");
      log.error("Error processing link @ Product Count : {} | {}", link, matcher.find());
    }
  }

  private void getSearchPageStatus(Link link, String pageText) {
    String regex = "[0-9]+ Product[s]*";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(pageText);

    if (matcher.find()) {
      String countStr = matcher.group(0).split(" ")[0];
      int productCount = Integer.parseInt(countStr);
      link.setProductCount(productCount);
      if (productCount > 0) link.setProcessFlag(LinkStatusEnum.SUCCESS);
      else link.setProcessFlag(LinkStatusEnum.INVALID_COUNT);
    } else {
      link.setProcessFlag(LinkStatusEnum.FATAL);
      link.setErrorMessage("Page Error : Failed to Find Search Product Count");
      log.error("Error processing link @ Search Product Count : {} | {}", link, matcher.find());
    }
  }

  private List<String> extractPageUrls(Page page) {
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

  private List<String> getPageUrls(Link link, List<String> urls) {
    final String startPath = UrlUtil.getStartPath(link.getBaseUrl());
    return urls.stream()
        .filter(
            url ->
                url.startsWith(startPath)
                    && !url.substring(startPath.length()).isEmpty()
                    && !UrlUtil.getPageType(url).equals(PageTypeEnum.PRODUCT))
        .map(url -> url.substring(startPath.length()))
        .toList();
  }
}
