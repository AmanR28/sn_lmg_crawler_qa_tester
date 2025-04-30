package com.lmg.crawler_qa_tester.service;

public interface ProjectService {
    // Should use Event Bus, instead of directly managing Consumer
    Integer createProject();

    // Should use Event Bus, instead of directly managing Consumer
    void initProject();

    // Should use Event Bus, instead of directly managing Consumer
    void startProject();

    // Should use Event Bus, instead of directly managing Consumer
    void stopProject();

    // Should use Event Bus, instead of directly managing Consumer
    void cancelProject();
}

