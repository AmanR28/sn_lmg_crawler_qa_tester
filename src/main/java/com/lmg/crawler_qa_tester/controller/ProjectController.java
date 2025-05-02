package com.lmg.crawler_qa_tester.controller;

import com.lmg.crawler_qa_tester.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects")
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

    @PutMapping("/start")
    public void start() {

        projectService.startProject();
    }

    @PutMapping("/stop")
    public String stop() {
        projectService.stopProject();
        return "OK";
    }

    @DeleteMapping()
    public String cancel() {
        projectService.cancelProject();
        return "OK";
    }

}

