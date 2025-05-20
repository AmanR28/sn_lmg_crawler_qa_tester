package com.lmg.crawler_qa_tester.controller;

import com.lmg.crawler_qa_tester.constants.ReportStatus;
import com.lmg.crawler_qa_tester.dto.GenerateReportRequest;
import com.lmg.crawler_qa_tester.repository.entity.ReportEntity;
import com.lmg.crawler_qa_tester.service.GenerateReportService;
import com.lmg.crawler_qa_tester.service.GetReportStatusService;
import com.lmg.crawler_qa_tester.service.ReportService;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/reports")
public class ReportController {
  @Autowired
  ReportService reportService;
  @Autowired
  GenerateReportService generateReportService;
  @Autowired
  GetReportStatusService getReportStatusService;

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
  @PostMapping("/generateReport")
  public ResponseEntity<Object> generateReport(@RequestBody GenerateReportRequest reportRequest)
  {
    Pair<String, String> reportStatus= generateReportService.generateReport(reportRequest);
    Map<String , Object > response =  new HashMap<>();
    String status = reportStatus.getFirst();
    String reportId = reportStatus.getSecond();
    Long id;
    try
    {
      id = Long.valueOf(reportId);
    }
    catch (Exception e)
    {
      id = null;
    }

    response.put("Status",status);
    response.put("reportId",id);
    return switch (status) {
          case "OK" -> new ResponseEntity<>(response, HttpStatus.OK);
          case "Not Found" -> new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
          case "Already Exists" -> new ResponseEntity<>(response, HttpStatus.ALREADY_REPORTED);
          default -> new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
      };
  }
  @GetMapping("/getReportStatus/{id}")
  public ResponseEntity<Object> getReportStatus(@PathVariable @NotBlank  Integer id )
  {
    Optional<ReportEntity> entity =  getReportStatusService.getReportStatus(id);
    if(entity.isEmpty())
    {
      return new ResponseEntity<>("Not Found", HttpStatus.NOT_FOUND);
    }
    Map<String,Object> res= new HashMap<>();
    res.put("report_id",entity.get().getId());
    res.put("Status", ReportStatus.fromCode(entity.get().getStatus().toString()));
    return new ResponseEntity<>(res,HttpStatus.OK);
  }
}
