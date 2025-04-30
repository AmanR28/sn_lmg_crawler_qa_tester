package com.lmg.crawler_qa_tester.service.impl;

import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl {
    private Boolean isRunning = false;
    private Integer projectId = null;

    public Integer initProject() {
        // Create Project Record
        // Create Project Status Record
        // Init Domain Beans
        return null;
    };

    public void startProject() {
        // Set isRunning and ProjectId
        // Update Project Status Record
        // Start Adapters
        return;
    }

    public void stopProject() {
        // Set isRunning and ProjectId
        // Update Project Status Record
        // Stop Adapters
        return;
    }
}
