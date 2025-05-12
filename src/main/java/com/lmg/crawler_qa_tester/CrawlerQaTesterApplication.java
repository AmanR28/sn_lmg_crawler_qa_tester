package com.lmg.crawler_qa_tester;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableIntegration
@EnableAsync
public class CrawlerQaTesterApplication {

  public static void main(String[] args) {

    SpringApplication.run(CrawlerQaTesterApplication.class, args);
  }
}
