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
  public Integer create(@RequestBody ProjectRequest request) {
    return processService.createProject(request.getCompareFrom(), request.getCompareTo());
  }
}
