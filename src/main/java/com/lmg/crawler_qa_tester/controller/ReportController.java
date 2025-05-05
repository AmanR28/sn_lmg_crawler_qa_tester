package com.lmg.crawler_qa_tester.controller;

import com.lmg.crawler_qa_tester.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
public class ReportController {
  @Autowired ReportService reportService;

  @PostMapping("/{process_id}")
  public ResponseEntity<Object> generateComparisonReport(@PathVariable("process_id") Integer processId) {
    try {
      byte[] comparisonReport = reportService.generateComparisonReport(processId);
      HttpHeaders headers = new HttpHeaders();
      headers.add(
          "Content-Disposition", "attachment; filename=comparison_report_" + processId + ".csv");
      headers.add("Content-Type", "text/csv");

      return new ResponseEntity<>(comparisonReport, headers, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(
          "Error generating CSV: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
