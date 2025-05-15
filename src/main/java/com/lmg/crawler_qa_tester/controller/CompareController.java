package com.lmg.crawler_qa_tester.controller;

import com.lmg.crawler_qa_tester.dto.comparator.CompareRequest;
import com.lmg.crawler_qa_tester.service.comparator.CompareService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("lmg/crawlerService/env-comparator/compare")
public final class CompareController {
    private final CompareService compareService;

    public CompareController(CompareService compareService) {
        this.compareService = compareService;
    }

    @PostMapping
    public ResponseEntity<String> compare(@RequestBody @Validated CompareRequest request) {
        long startTime = System.currentTimeMillis();
        ResponseEntity<String> response = compareService.compare(request);
        long endTime = System.currentTimeMillis();
        
        String originalBody = response.getBody();
        String updatedBody = originalBody + "\nSheet generation time: " + (endTime - startTime) + "ms";
        
        return ResponseEntity
                .status(response.getStatusCode())
                .headers(response.getHeaders())
                .body(updatedBody);
    }
}
