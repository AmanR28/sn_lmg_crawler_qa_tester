package com.lmg.crawler_qa_tester.repository.custom;

import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;

import java.util.List;

public interface CrawlerDetailRepositoryCustom {
  void saveNewLinks(List<CrawlDetailEntity> crawlDetailEntityList);
}
