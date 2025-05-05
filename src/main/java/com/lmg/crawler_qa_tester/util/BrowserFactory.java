package com.lmg.crawler_qa_tester.util;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class BrowserFactory {
    @Autowired
    private Environment environment;

    public List<String> getOptions() {
        return Arrays.asList(new String[]{
                "--disable-blink-features=AutomationControlled",
                "--no-sandbox",
                "--disable-dev-shm-usage"
        });
    }

    public Browser getProdWebDriver() {
        boolean isHeadless = environment.getProperty("env.prod.browserHeadless", Boolean.class, true);
        Playwright playwright = Playwright.create();
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(isHeadless)
                .setArgs(getOptions());
        log.info("Starting Prod Browser with headless: {}", isHeadless);
        return playwright.chromium().launch(launchOptions);
    }

    public Browser getPreProdWebDriver() {
        boolean isHeadless = environment.getProperty("env.preprod.browserHeadless", Boolean.class, true);
        Playwright playwright = Playwright.create();
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(isHeadless)
                .setArgs(getOptions());
        log.info("Starting PreProd Browser with headless: {}", isHeadless);
        return playwright.chromium().launch(launchOptions);
    }

    public Page getProdBrowserPage(Browser browser) {
        return browser.newContext(new Browser.NewContextOptions()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .setViewportSize(1920, 1080)
        ).newPage();
    }

    public Page getPreProdBrowserPage(Browser browser) {
        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .setViewportSize(1920, 1080));
        Cookie cookie = new Cookie("preprod", "true");
        cookie.setDomain("blc.centrepointstores.com");
        cookie.setPath("/");
        context.addCookies(List.of(cookie));
        return context.newPage();
    }
}
