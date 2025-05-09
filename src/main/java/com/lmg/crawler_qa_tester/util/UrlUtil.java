package com.lmg.crawler_qa_tester.util;

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
}
