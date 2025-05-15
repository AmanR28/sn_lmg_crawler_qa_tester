package com.lmg.crawler_qa_tester.repository.internal;

import com.lmg.crawler_qa_tester.dto.Link;
import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrawlDetailRepository extends JpaRepository<CrawlDetailEntity, Integer> {
  List<CrawlDetailEntity> getCrawlDetailEntitiesByCrawlHeaderId(Integer processId);

  int countCrawlDetailEntitiesByCrawlHeaderId(Integer processId);

  List<CrawlDetailEntity> findAllByCrawlHeaderId(Integer crawlHeaderId);

  @Modifying
  @Query(
      "UPDATE CrawlDetailEntity c SET c.processFlag = :newFlag WHERE c.crawlHeaderId = :headerId AND c.processFlag = :currentFlag")
  @Transactional
  int batchUpdateProgressFlag(
      @Param("headerId") Integer headerId,
      @Param("currentFlag") String currentFlag,
      @Param("newFlag") String newFlag);
}
