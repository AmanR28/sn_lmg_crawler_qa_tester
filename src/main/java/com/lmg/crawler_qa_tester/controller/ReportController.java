package com.lmg.crawler_qa_tester.controller;

import com.lmg.crawler_qa_tester.service.CompareLinkService;
import jakarta.websocket.server.PathParam;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
public class ReportController {
  @Autowired
  private CompareLinkService compareLinkService;

  @GetMapping("/{id}")
  public ResponseEntity<Object> getReport(@PathVariable("id") Integer id) {
    try{
      byte[] comparisonReport = compareLinkService.getCompareReports(id);
      HttpHeaders headers = new HttpHeaders();
      headers.add("Content-Disposition", "attachment; filename=comparison_link" + id + ".csv");
      headers.add("Content-Type", "text/csv");

      return new ResponseEntity<>(comparisonReport, headers, HttpStatus.OK);
    }
    catch (Exception e)
    {
      return new ResponseEntity<>("Error generating CSV: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
