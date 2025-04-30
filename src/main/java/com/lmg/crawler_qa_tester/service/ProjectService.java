package com.lmg.crawler_qa_tester.service;

import org.springframework.stereotype.Service;

@Service
public class ProjectService {
    private Boolean isRunning = false;
    private Integer projectId = null;

    // Should use Event Bus, instead of directly managing Consumer
    public Integer initProject() {
        // Create Project Record
        // Create Project Status Record
        return null;
    };

    // Should use Event Bus, instead of directly managing Consumer
    public void startProject() {
        // Set isRunning and ProjectId
        // Using DB, Init Domain Beans & Report Bean
        // Update Project Status Record
        // Start Adapters
    }

    // Should use Event Bus, instead of directly managing Consumer
    public void stopProject() {
        // Stop Adapters
        // Set isRunning and ProjectId
        // Update Project Status Record
    }

    // Should use Event Bus, instead of directly managing Consumer
    public void cancelProject() {
        // Stop Adapters
        // Set isRunning and ProjectId
        // Update Project Status Record
    }
}

