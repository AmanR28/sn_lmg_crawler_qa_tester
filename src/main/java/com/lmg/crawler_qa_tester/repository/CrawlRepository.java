package com.lmg.crawler_qa_tester.repository;

import com.lmg.crawler_qa_tester.dto.Link;
import com.lmg.crawler_qa_tester.dto.Process;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface CrawlRepository {
  boolean hasActiveProcess();

  void saveProcess(Process process);

  void saveAllProcesses(List<Process> processes);

  List<Link> getLinksByProcessId(Integer processId);

  void saveLink(Link link);

  void saveNewLinks(List<Link> links);
}
