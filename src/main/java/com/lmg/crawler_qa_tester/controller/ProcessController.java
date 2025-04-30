package com.lmg.crawler_qa_tester.controller;

import org.springframework.web.bind.annotation.*;

@RestController
public class ProcessController {
    // Status of Crawler and Report
    @GetMapping()
    public String status() {
        return "OK";
    }

    // Init the process & return Project Id
    @PostMapping()
    public Integer create() {
        return 1;
    }

    @PutMapping()
    public String start() {
        return "OK";
    }

    @PutMapping()
    public String stop() {
        return "OK";
    }

    @DeleteMapping()
    public String cancel() {
        return "OK";
    }
}

