package com.lmg.crawler_qa_tester.dto;

import com.lmg.crawler_qa_tester.constants.EnvironmentEnum;
import com.lmg.crawler_qa_tester.constants.LinkStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Link {
  private Integer id;
  private Integer crawlHeaderId;
  private EnvironmentEnum env;
  private String baseUrl;
  private String path;
  private LinkStatusEnum processFlag;
  private String errorMessage;
  private int depth;
}
