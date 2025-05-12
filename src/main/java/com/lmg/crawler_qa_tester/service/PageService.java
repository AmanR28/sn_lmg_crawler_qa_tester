package com.lmg.crawler_qa_tester.service;

import com.lmg.crawler_qa_tester.constants.LinkStatusEnum;
import com.lmg.crawler_qa_tester.constants.PageTypeEnum;
import com.lmg.crawler_qa_tester.dto.Link;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.uri.Uri;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PageService {
  public void processPageStatus(Page page, Link link, Response response) {
    getPageStatus(link, page, response);
    PageTypeEnum pageType = getPageType(link.getPath());
    if (pageType == PageTypeEnum.CATEGORY) getCategoryPageStatus(link, page);
  }

  public List<String> processPageData(Page page, Link link) {
    final String startPath = Uri.create(link.getBaseUrl()).getPath();
    List<String> urls = getPageUrls(page);

    List<String> baseFilteredUrls =
        urls.stream()
            .filter(
                url -> url.startsWith(startPath) && !getPageType(url).equals(PageTypeEnum.PRODUCT))
            .map(url -> url.substring(startPath.length()))
            .toList();
    return baseFilteredUrls;
  }

  private PageTypeEnum getPageType(String path) {
    List<String> pathSlices = List.of(path.split("/"));

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

  private void getCategoryPageStatus(Link link, Page page) {}

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
