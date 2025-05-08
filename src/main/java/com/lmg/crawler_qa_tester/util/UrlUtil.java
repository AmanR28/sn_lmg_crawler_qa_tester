package com.lmg.crawler_qa_tester.util;

import org.asynchttpclient.uri.Uri;

public class UrlUtil {
  public static String getDomain(String url) {
    return Uri.create(url).getHost();
  }

  public static String getCountry(String url) {
    return Uri.create(url).getPath().split("/")[0];
  }

  public static String getLocale(String url) {
    return Uri.create(url).getPath().split("/")[1];
  }
}
