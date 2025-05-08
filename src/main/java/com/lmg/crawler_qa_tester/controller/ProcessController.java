package com.lmg.crawler_qa_tester.controller;

import com.lmg.crawler_qa_tester.dto.ProjectRequest;
import com.lmg.crawler_qa_tester.service.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/process")
public class ProcessController {
  @Autowired private ProcessService processService;

  @PostMapping()
  public void create(@RequestBody ProjectRequest request) {
    processService.createProject(request.getCompareFromBaseUrl(), request.getCompareToBaseUrl());
  }
}
