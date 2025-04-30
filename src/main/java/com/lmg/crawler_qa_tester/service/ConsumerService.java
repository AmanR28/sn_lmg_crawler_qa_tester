package com.lmg.crawler_qa_tester.service;

import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Service;

@Service
public class ConsumerService {
    @ServiceActivator()
    public void consumeProd() {
        // Get Driver
        // Crawler Service - Get HTML
        // Page Service - Page Status Validation
        // Page Service - Process Page & Get Links
        // Save Links
    }

    @ServiceActivator()
    public void consumePreProd() {

    }

}
