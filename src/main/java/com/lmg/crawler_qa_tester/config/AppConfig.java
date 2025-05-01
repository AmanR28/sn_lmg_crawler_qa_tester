package com.lmg.crawler_qa_tester.config;

import lombok.Data;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class AppConfig {
    private Boolean isRunning = false;
    private Integer runningProjectId = null;

}
