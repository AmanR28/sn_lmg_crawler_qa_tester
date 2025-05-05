package com.lmg.crawler_qa_tester.repository.entity;

import com.lmg.crawler_qa_tester.constants.LinkStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "crawl_detail",
    uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "env", "base_url", "path"}),
    indexes = @Index(name = "idx_process_flag", columnList = "process_flag"))
public class CrawlDetailEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NonNull
    @Column(name = "project_id")
    private Integer crawlHeaderId;

    @NonNull
    @Column(name = "env")
    private String env;

    @NonNull
    @Column(name = "base_url")
    private String baseUrl;

    @NonNull
    @Column(name = "path")
    private String path;

    @NonNull
    @Column(name = "process_flag")
    private String processFlag = LinkStatus.NOT_PROCESSED.getValue();

    @Column(name = "error_message")
    private String errorMessage;

}
