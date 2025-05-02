package com.lmg.crawler_qa_tester.service;

import com.lmg.crawler_qa_tester.config.AppConfig;
import com.lmg.crawler_qa_tester.constants.ConsumerStatusEnum;
import com.lmg.crawler_qa_tester.dto.Domain;
import com.lmg.crawler_qa_tester.repository.LinkRepository;
import com.lmg.crawler_qa_tester.repository.ProjectRepository;
import com.lmg.crawler_qa_tester.repository.entity.LinkEntity;
import com.lmg.crawler_qa_tester.repository.entity.ProjectEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private LinkRepository linkRepository;
    @Autowired
    private Domain prodDomain;

    private Integer id = null;

    public Integer createProject() {
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setProdBaseUrl("https://www.centrepointstores.com/kw/en");
        projectEntity.setPreProdBaseUrl("https://blc.centrepointstores.com/kw/en");
        projectEntity.setProdProcessStatus(ConsumerStatusEnum.INIT);
        projectEntity.setPreProdProcessStatus(ConsumerStatusEnum.INIT);
        ProjectEntity savedEntity = projectRepository.save(projectEntity);

        LinkEntity prodLink = new LinkEntity();
        prodLink.setProjectId(savedEntity.getId());
        prodLink.setBaseUrl(savedEntity.getProdBaseUrl());
        prodLink.setUrl("/");
        linkRepository.save(prodLink);

        this.id = savedEntity.getId();

        return savedEntity.getId();
    }

    public void startProject() {

        if (appConfig.getIsRunning()) {
            throw new RuntimeException("Project is already running");
        }
        appConfig.setIsRunning(true);
        appConfig.setRunningProjectId(this.id);

        ProjectEntity project = projectRepository.getReferenceById(1);
        prodDomain.setBaseUrl(project.getProdBaseUrl());

        project.setProdProcessStatus(ConsumerStatusEnum.RUNNING);
        projectRepository.save(project);

        //TODO: Start Adapters
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

