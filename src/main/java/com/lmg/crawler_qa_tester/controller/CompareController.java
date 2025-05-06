package com.lmg.crawler_qa_tester.controller;


import com.lmg.crawler_qa_tester.dto.comparator.CompareRequest;
import com.lmg.crawler_qa_tester.service.comparator.CompareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("lmg/crawlerService/env-comparator/compare")
public class CompareController {
    @Autowired
    private CompareService compareService;

    @PostMapping
    public ResponseEntity<String> compare(@RequestBody @Validated CompareRequest request) throws Exception {
        compareService.compare(request);
        return ResponseEntity.ok("Comparison completed. Excel generated.");
    }
}
