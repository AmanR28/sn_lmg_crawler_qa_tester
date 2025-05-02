package com.lmg.crawler_qa_tester.controller;

import com.lmg.crawler_qa_tester.service.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects")
public class ProcessController {
    @Autowired
    private ProcessService processService;

    @GetMapping()
    public String status() {

        return "OK";
    }

    @PostMapping()
    public Integer create() {

        return processService.createProject();
    }

    @PutMapping("/start")
    public void start() {

        processService.startProject();
    }

    @PutMapping("/stop")
    public String stop() {
        processService.stopProject();
        return "OK";
    }

    @DeleteMapping()
    public String cancel() {
        processService.cancelProject();
        return "OK";
    }

}

