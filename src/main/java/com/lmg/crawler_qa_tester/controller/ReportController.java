package com.lmg.crawler_qa_tester.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
public class ReportController {
    // Get Report for given ProjectId
    @RequestMapping()
    public String getReport(Integer Projectid) {
        return "OK";
    }
}
