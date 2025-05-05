package com.lmg.crawler_qa_tester.controller;

import com.lmg.crawler_qa_tester.dto.ProjectRequest;
import com.lmg.crawler_qa_tester.service.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects")
public class ProcessController {
  @Autowired private ProcessService processService;

  @GetMapping()
  public String status() {

    return processService.getStatus();
  }

  @PostMapping()
  public Integer create(@RequestBody ProjectRequest request) {

    return processService.createProject(request.getProdBaseUrl(), request.getPreProdBaseUrl());
  }

  @PutMapping("/start/{projectId}")
  public void start(@PathVariable Integer projectId) {

    processService.startProject(projectId);
  }
}
