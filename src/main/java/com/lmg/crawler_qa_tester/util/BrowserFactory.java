package com.lmg.crawler_qa_tester.util;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BrowserFactory {
    @Value("${env.app.blockMedia}")
    private boolean blockMedia;

    @Value("${env.app.blockAnalytics}")
    private boolean blockAnalytics;

  private static final Set<String> BLOCKED_TYPES = Set.of("image", "media", "stylesheet", "font");
  private static final List<String> BLOCKED_ANALYTICS_DOMAINS =
      List.of(
          "google-analytics.com",
          "googletagmanager.com",
          "facebook.net",
          "facebook.com/tr",
          "tiktok.com",
          "snapchat.com",
          "doubleclick.net",
          "adsbygoogle.js",
          "bing.com",
          "yahoo.com",
          "criteo.com",
          "adnxs.com");

  @Autowired private Environment environment;

  public List<String> getOptions() {
    return Arrays.asList(
        "--disable-blink-features=AutomationControlled", "--no-sandbox", "--disable-dev-shm-usage");
  }

  public Browser getBrowser() {
    boolean isHeadless = environment.getProperty("env.app.browserHeadless", Boolean.class, true);
    Playwright playwright = Playwright.create();
    BrowserType.LaunchOptions launchOptions =
        new BrowserType.LaunchOptions().setHeadless(isHeadless).setArgs(getOptions());
    return playwright.chromium().launch(launchOptions);
  }

  public Page getPage(Browser browser, String domain) {
    BrowserContext context =
        browser.newContext(
            new Browser.NewContextOptions()
                .setUserAgent(
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .setViewportSize(1920, 1080));

    Cookie cookie = new Cookie("preprod", "true").setDomain("." + domain).setPath("/");
    context.addCookies(List.of(cookie));

    Page page = context.newPage();
    page.route(
        "**/*",
        route -> {
          String resourceType = route.request().resourceType();
          String url = route.request().url();

          boolean isBlockedType = blockMedia && BLOCKED_TYPES.contains(resourceType);
          boolean isAnalytics = blockAnalytics && BLOCKED_ANALYTICS_DOMAINS.stream().anyMatch(url::contains);

          if (isBlockedType || isAnalytics) {
            route.abort();
          } else {
            route.resume();
          }
        });
    return page;
  }
}
