package com.lmg.crawler_qa_tester.dto;

import lombok.Data;

@Data
public class ProjectRequest {
  private String compareToBaseUrl;
  private String compareFromBaseUrl;
}
