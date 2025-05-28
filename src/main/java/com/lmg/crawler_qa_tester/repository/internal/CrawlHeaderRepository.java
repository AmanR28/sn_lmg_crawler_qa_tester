package com.lmg.crawler_qa_tester.repository.internal;

import com.lmg.crawler_qa_tester.constants.ProcessStatusEnum;
import com.lmg.crawler_qa_tester.repository.entity.CrawlHeaderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrawlHeaderRepository extends JpaRepository<CrawlHeaderEntity, Integer> {
  int countByStatus(String status);

  CrawlHeaderEntity findByStatus(String value);

  default boolean hasActiveProcess() {
    return (this.countByStatus(ProcessStatusEnum.NEW.getValue()) > 0
        || this.countByStatus(ProcessStatusEnum.RUNNING.getValue()) > 0);
  }

  @Query(
      "SELECT cHeader.id FROM CrawlHeaderEntity  cHeader WHERE cHeader.domain = :domain AND cHeader.country = :country AND cHeader.locale = :locale AND cHeader.department = :department")
  Optional<Integer> findCrawlHeaderId(
      @Param("domain") String domain,
      @Param("country") String country,
      @Param("locale") String locale,
      @Param("department") String department);

  Optional<CrawlHeaderEntity> findById(Integer id);
}
