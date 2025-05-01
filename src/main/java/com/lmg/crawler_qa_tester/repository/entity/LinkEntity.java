package com.lmg.crawler_qa_tester.repository.entity;

import com.lmg.crawler_qa_tester.constants.LinkStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "links")
public class LinkEntity {
    @Id
    private Integer id;
    @NonNull
    private Integer projectId;
    @NonNull
    private String baseUrl;
    @NonNull
    private String url;
    @NonNull
    private String processFlag = "N";
    @NonNull
    private LinkStatus linkStatus = LinkStatus.NEW;
}
