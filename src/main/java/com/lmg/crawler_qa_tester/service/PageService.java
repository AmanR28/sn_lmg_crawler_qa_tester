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
    public Boolean validatePageStatus(Page page) {
        return true;
    }

    public List<String> processPage(Page page) {

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
