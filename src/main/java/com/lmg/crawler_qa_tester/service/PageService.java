package com.lmg.crawler_qa_tester.service;

import com.lmg.crawler_qa_tester.constants.LinkStatusEnum;
import com.lmg.crawler_qa_tester.constants.PageTypeEnum;
import com.lmg.crawler_qa_tester.dto.Link;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.uri.Uri;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PageService {
  @Value("${env.app.pageWait}")
  int PAGE_WAIT;

  public List<String> processPageData(Page page, Link link) {
    PageTypeEnum pageType = getPageType(link.getPath());

    Response response = page.navigate(link.getBaseUrl() + link.getPath());
    page.waitForTimeout(PAGE_WAIT);
    String pageText = page.innerText("body");
    List<String> urls = getPageUrls(page);

    getPageStatus(link, page, response);
    if (pageType == PageTypeEnum.CATEGORY) getCategoryPageStatus(link, pageText);
    if (!link.getProcessFlag().equals(LinkStatusEnum.SUCCESS)) return null;

    if (pageType.equals(PageTypeEnum.CATEGORY)) return null;
    final String startPath = Uri.create(link.getBaseUrl()).getPath();
    return urls.stream()
        .filter(
            url ->
                url.startsWith(startPath)
                    && !url.substring(startPath.length()).isEmpty()
                    && !getPageType(url).equals(PageTypeEnum.PRODUCT))
        .map(url -> url.substring(startPath.length()))
        .toList();
  }

  private void getCategoryPageStatus(Link link, String pageText) {
    String regex = "[1-9][0-9]* Product[s]*";
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
    }
  }

  private PageTypeEnum getPageType(String path) {
    List<String> pathSlices = List.of(path.split("/"));
    if (pathSlices.size() < 2) return PageTypeEnum.OTHER;

    if (pathSlices.get(1).equals("department")) {
      return PageTypeEnum.DEPARTMENT;
    } else if (pathSlices.get(1).equals("c")) {
      return PageTypeEnum.CATEGORY;
    } else if (pathSlices.size() > 3 && pathSlices.get(pathSlices.size() - 2).equals("p")) {
      return PageTypeEnum.PRODUCT;
    } else {
      return PageTypeEnum.OTHER;
    }
  }

  private void getPageStatus(Link link, Page page, Response response) {
    int status = response.status();
    if (status >= 200 && status < 300) {
      link.setProcessFlag(LinkStatusEnum.SUCCESS);
    } else if (status >= 400 && status < 500) {
      link.setProcessFlag(LinkStatusEnum.NOT_FOUND);
    } else if (status >= 500 && status < 600) {
      link.setProcessFlag(LinkStatusEnum.FATAL);
    }
  }

  private List<String> getPageUrls(Page page) {

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
}
