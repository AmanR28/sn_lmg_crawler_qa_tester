package com.lmg.crawler_qa_tester.service;

import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PageService {
    public Boolean validatePageStatus(WebDriver driver) {

        return true;
    }

    // Return Urls only, not Links
    // TO BE THOUGHT FOR Special Data
    public List<String> processPage() {

        return null;
    }

}
