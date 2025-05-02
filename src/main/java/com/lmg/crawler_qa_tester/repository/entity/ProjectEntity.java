package com.lmg.crawler_qa_tester.repository.entity;

import com.lmg.crawler_qa_tester.constants.ConsumerStatusEnum;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "projects")
public class ProjectEntity {
    @Id
    @GeneratedValue
    private Integer id;

    @CreationTimestamp
    private Timestamp createdTime;
    @UpdateTimestamp
    private Timestamp updatedTime;

    @NonNull
    private String prodBaseUrl;
    @NonNull
    private String preProdBaseUrl;

    private ConsumerStatusEnum prodProcessStatus = ConsumerStatusEnum.INIT;
    private ConsumerStatusEnum preProdProcessStatus = ConsumerStatusEnum.INIT;

}
