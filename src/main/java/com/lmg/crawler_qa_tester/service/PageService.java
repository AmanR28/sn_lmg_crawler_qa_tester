package com.lmg.crawler_qa_tester.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PageService {
    public Boolean validatePageStatus(WebDriver driver) {
        return true;
    }

    // Return Urls only, not Links
    // TO BE THOUGHT FOR Special Data
    public List<String> processPage(Browser browser, String url) {

        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .setViewportSize(1920, 1080)
        );

        Page page = context.newPage();
        page.navigate(url);
        page.waitForTimeout(3000);

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
