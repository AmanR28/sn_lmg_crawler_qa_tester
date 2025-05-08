package com.lmg.crawler_qa_tester.repository;

import com.lmg.crawler_qa_tester.dto.Link;
import com.lmg.crawler_qa_tester.dto.Process;
import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface CrawlRepository {
  boolean hasActiveProcess();

  void saveNewProcesses(List<Process> processes);

  void saveNewLinks(List<Link> links);

  void updateProcess(Process process);
}
