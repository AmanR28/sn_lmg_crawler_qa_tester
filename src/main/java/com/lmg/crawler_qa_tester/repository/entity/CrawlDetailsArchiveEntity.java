package com.lmg.crawler_qa_tester.repository.entity;

import com.lmg.crawler_qa_tester.constants.LinkStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "crawl_details_archive")
public class CrawlDetailsArchiveEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NonNull
    @Column(name = "crawl_header_id")
    private Integer crawl_header_id;

    @NonNull
    @Column(name = "env")
    private String env;

    @NonNull
    @Column(name = "base_url")
    private String baseUrl;

    @NonNull
    @Column(name = "url")
    private String path;

    @NonNull
    @Column(name = "process_flag")
    private LinkStatus processFlag = LinkStatus.NEW;

    @NonNull
    @Column(name = "error_message")
    private String errorMessage;
}
