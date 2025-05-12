package com.lmg.crawler_qa_tester.repository;

import com.lmg.crawler_qa_tester.repository.custom.CrawlerDetailRepositoryCustom;
import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrawlDetailRepository
    extends JpaRepository<CrawlDetailEntity, Integer>, CrawlerDetailRepositoryCustom {
  void saveNewLinks(List<CrawlDetailEntity> crawlDetailEntityList);

  List<CrawlDetailEntity> findAllByCrawlHeaderId(Integer crawlHeaderId);
  @Query("SELECT c FROM Crawl c WHERE c.base_url = :host AND c.country = :country AND c.locale = :locale AND c.department = :department ORDER BY c.crawlTimestamp DESC")
  Optional<CrawlDetailEntity> findLatestCrawl(
          @Param("host") String host,
          @Param("country") String country,
          @Param("locale") String locale,
          @Param("department") String department
  );
}
