package com.lmg.crawler_qa_tester.repository.entity;

import com.lmg.crawler_qa_tester.constants.ProcessStatusEnum;
import jakarta.persistence.*;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
  @Column(name = "compareFromUrl")
  private String compareFrom;

  @NonNull
  @Column(name = "compareToUrl")
  private String compareTo;

  @Column(name = "status")
  private String status = ProcessStatusEnum.INIT.getValue();

  @NonNull
  @Column(name = "host_name")
  private String hostName;

  @NonNull
  @Column(name = "country")
  private String country;

  @NonNull
  @Column(name = "locale")
  private String locale;

  @NonNull
  @Column(name = "department")
  private String department;

  @CreationTimestamp
  @Column(name = "created_time")
  private Timestamp createdTime;

  @UpdateTimestamp
  @Column(name = "updated_time")
  private Timestamp updatedTime;

  @Column(name = "consumer_thread")
  private int consumerThread;

  @Column(name = "page_count")
  private int pageCount;
}
