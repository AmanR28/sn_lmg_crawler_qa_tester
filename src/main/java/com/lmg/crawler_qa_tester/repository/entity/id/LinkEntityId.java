package com.lmg.crawler_qa_tester.repository.entity.id;

import lombok.Data;

import java.io.Serializable;

@Data
public class LinkEntityId implements Serializable {
    private Integer projectId;
    private String url;
}
