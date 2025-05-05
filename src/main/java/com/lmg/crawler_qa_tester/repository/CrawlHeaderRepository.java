package com.lmg.crawler_qa_tester.repository;

import com.lmg.crawler_qa_tester.constants.ConsumerStatusEnum;
import com.lmg.crawler_qa_tester.repository.entity.CrawlHeaderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrawlHeaderRepository extends JpaRepository<CrawlHeaderEntity, Integer> {
  int countByStatus(String status);

  default boolean hasActiveProcess() {
    return (this.countByStatus(ConsumerStatusEnum.INIT.getValue()) > 0
        || this.countByStatus(ConsumerStatusEnum.RUNNING.getValue()) > 0);
  }
}
