package com.lmg.crawler_qa_tester.repository.internal;

import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CrawlDetailRepository extends JpaRepository<CrawlDetailEntity, Integer> {
  List<CrawlDetailEntity> getCrawlDetailEntitiesByCrawlHeaderId(Integer processId);

  List<CrawlDetailEntity> findAllByCrawlHeaderId(Integer crawlHeaderId);
  @Query("SELECT c FROM CrawlDetailEntity c WHERE c.crawlHeaderId = :crawlHeaderId AND c.depth <= :maxDepth")
  List<CrawlDetailEntity> findByCrawlHeaderIdAndDepthLessThanEqual(@Param("crawlHeaderId") Integer crawlHeaderId,
                                                                   @Param("maxDepth") Integer maxDepth);


  @Modifying
  @Query(
      "UPDATE CrawlDetailEntity c SET c.processFlag = :newFlag WHERE c.crawlHeaderId = :headerId AND c.processFlag = :currentFlag")
  @Transactional
  int batchUpdateProgressFlag(
      @Param("headerId") Integer headerId,
      @Param("currentFlag") String currentFlag,
      @Param("newFlag") String newFlag);
}
