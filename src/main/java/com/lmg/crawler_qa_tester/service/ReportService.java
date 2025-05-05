package com.lmg.crawler_qa_tester.service;

import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Service;

@Service
public class ReportService {
  @ServiceActivator()
  public void genReport() {
    // Get What Report To Generate
    // For Each Report Type - Generate Report
  }

  void generate_PROD_LINKS_CSV() {
    // Write SQL that should return the final result for report - No data processing here
    // Write to CSV
  }

  void generateCSV() {}

  void generateExcel() {}
}
