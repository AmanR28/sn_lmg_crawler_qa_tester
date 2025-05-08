package com.lmg.crawler_qa_tester.repository.internal;

import com.lmg.crawler_qa_tester.constants.ProcessStatusEnum;
import com.lmg.crawler_qa_tester.repository.entity.CrawlHeaderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrawlHeaderRepository extends JpaRepository<CrawlHeaderEntity, Integer> {
  int countByStatus(String status);

  default boolean hasActiveProcess() {
    return (this.countByStatus(ProcessStatusEnum.NEW.getValue()) > 0
        || this.countByStatus(ProcessStatusEnum.RUNNING.getValue()) > 0);
  }
}
