package com.lmg.crawler_qa_tester.repository.entity;

import com.lmg.crawler_qa_tester.constants.LinkStatusEnum;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "crawl_detail",
    uniqueConstraints =
        @UniqueConstraint(
            name = "unique_link",
            columnNames = {"crawl_header_id", "env", "base_url", "path"}),
    indexes = @Index(name = "idx_process_flag", columnList = "process_flag"))
public class CrawlDetailEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @NonNull
  @Column(name = "crawl_header_id")
  private Integer crawlHeaderId;

  @NonNull
  @Column(name = "env")
  private String env;

  @NonNull
  @Column(name = "base_url")
  private String baseUrl;

  @NonNull
  @Column(name = "path", columnDefinition = "TEXT")
  private String path;

  @NonNull
  @Column(name = "process_flag")
  private String processFlag = LinkStatusEnum.NOT_PROCESSED.getValue();

  @Nullable
  @Column(name = "product_count")
  private Integer productCount;

  @Column(name = "parent_path")
  private String parentPath;

  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  @Column(name = "depth")
  private int depth = 0;
}
