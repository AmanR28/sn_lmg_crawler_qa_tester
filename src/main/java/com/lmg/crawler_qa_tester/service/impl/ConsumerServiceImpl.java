package com.lmg.crawler_qa_tester.service.impl;

import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Service;

@Service
public class ConsumerServiceImpl {
    @ServiceActivator()
    public void consumeProd() {
        // Get Driver
        // Crawler Service - Get Urls
        // Save Links
    }

    @ServiceActivator()
    public void consumePreProd() {

    }

}
