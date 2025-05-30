package com.lmg.crawler_qa_tester.repository;

import com.lmg.crawler_qa_tester.constants.LinkStatusEnum;
import com.lmg.crawler_qa_tester.constants.ProcessStatusEnum;
import com.lmg.crawler_qa_tester.dto.Link;
import com.lmg.crawler_qa_tester.dto.Process;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface CrawlRepository {
  boolean hasActiveProcess();

  Process saveProcess(Process process);

  void saveAllProcesses(List<Process> processes);

  int getLinkCountByProcessId(Integer processId);

  List<Link> getLinksByProcessId(Integer processId);

  void saveLink(Link link);

  void saveNewLinks(List<Link> links);

  void resetInProgressLinks(Integer id, LinkStatusEnum fromFlag, LinkStatusEnum toFlag);

  Process getProcessByStatus(ProcessStatusEnum processStatusEnum);
}
