package com.lmg.crawler_qa_tester.util;

import com.lmg.crawler_qa_tester.constants.PageTypeEnum;
import java.util.List;
import org.asynchttpclient.uri.Uri;

public class UrlUtil {
  public static String getDomain(String url) {
    return Uri.create(url).getHost().split("\\.", 2)[1];
  }

  public static String getCountry(String url) {
    return Uri.create(url).getPath().split("/")[1];
  }

  public static String getLocale(String url) {
    return Uri.create(url).getPath().split("/")[2];
  }

  public static String getStartPath(String url) {
    return Uri.create(url).getPath();
  }

  public static PageTypeEnum getPageType(String path) {
    List<String> pathSlices = List.of(path.split("/"));
    if (pathSlices.size() < 2) return PageTypeEnum.OTHER;

    if (pathSlices.get(1).equals("department")) {
      return PageTypeEnum.DEPARTMENT;
    } else if (pathSlices.get(1).equals("c")) {
      return PageTypeEnum.CATEGORY;
    } else if (pathSlices.get(1).startsWith("search")) {
      return PageTypeEnum.SEARCH;
    } else if (pathSlices.size() > 3 && pathSlices.get(pathSlices.size() - 2).equals("p")) {
      return PageTypeEnum.PRODUCT;
    } else {
      return PageTypeEnum.OTHER;
    }
  }
}
