package com.lmg.crawler_qa_tester.controller;

import com.lmg.crawler_qa_tester.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProjectController {
    @Autowired
    private ProjectService projectService;

    @GetMapping()
    public String status() {

        return "OK";
    }

    @PostMapping()
    public Integer create() {

        return projectService.createProject();
    }

    @PutMapping()
    public void start() {

        projectService.startProject();
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

