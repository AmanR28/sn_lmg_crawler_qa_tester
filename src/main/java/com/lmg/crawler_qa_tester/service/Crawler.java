package com.lmg.crawler_qa_tester.service;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;
import com.lmg.crawler_qa_tester.exception.PageAccessException;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class Crawler {
    public List<String> crawl(WebDriver driver, String url) {

        List<String> pageLinks = new ArrayList<>();
        driver.get(url);

        // Check if page is accessible by verifying the HTTP status code
        Long statusLong = (Long) ((JavascriptExecutor) driver).executeScript(
            "return window.performance.getEntries()[0].responseStatus || 0;");
        Integer status = statusLong != null ? statusLong.intValue() : 0;

        // If status is 0, it means the status code wasn't captured by the performance API
        // In this case, we'll check if the page title contains error indicators
        String pageTitle = driver.getTitle().toLowerCase();
        if (status == 0 && (pageTitle.contains("404") || pageTitle.contains("not found"))) {
            throw new PageAccessException("Page not accessible or returned error", url);
        }

        if (status == 404 || (status >= 500 && status < 600)) {
            throw new PageAccessException("Page returned HTTP error status", url, status);
        }

        List<WebElement> linkElements = driver.findElements(By.tagName("a"));
        for (WebElement element : linkElements) {
            String href = element.getAttribute("href");
            if (href != null && !href.isEmpty() && !href.startsWith("javascript:")
                && !href.startsWith("#")) {
                pageLinks.add(href);
            }
        }
        return pageLinks;
    }

}
