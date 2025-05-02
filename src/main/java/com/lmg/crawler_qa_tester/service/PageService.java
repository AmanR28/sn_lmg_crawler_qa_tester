package com.lmg.crawler_qa_tester.service;

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
    public List<String> processPage(WebDriver driver) {
        List<String> pageLinks = new ArrayList<>();
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
