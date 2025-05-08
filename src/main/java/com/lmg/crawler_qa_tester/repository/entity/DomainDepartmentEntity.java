package com.lmg.crawler_qa_tester.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "domain_department")
public class DomainDepartmentEntity {
  @Id
  @Column(name = "domain")
  String domain;

  @NonNull
  @Column(name = "departments")
  String departments;
}
