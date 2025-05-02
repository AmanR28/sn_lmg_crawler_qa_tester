package com.lmg.crawler_qa_tester.repository.entity;

import com.lmg.crawler_qa_tester.constants.ConsumerStatusEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "crawl_header")
public class CrawlHeaderEntity {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Integer id;

    @NonNull
    @Column(name = "prod_base_url")
    private String prodBaseUrl;

    @NonNull
    @Column(name = "pre_prod_base_url")
    private String preProdBaseUrl;

    @Column(name = "status")
    private String status = ConsumerStatusEnum.INIT.getValue();

    @CreationTimestamp
    @Column(name = "created_time")
    private Timestamp createdTime;

    @UpdateTimestamp
    @Column(name = "updated_time")
    private Timestamp updatedTime;
}
