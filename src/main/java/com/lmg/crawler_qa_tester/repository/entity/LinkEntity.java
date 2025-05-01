package com.lmg.crawler_qa_tester.repository.entity;

import com.lmg.crawler_qa_tester.constants.LinkStatus;
import com.lmg.crawler_qa_tester.repository.entity.id.LinkEntityId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Getter;
import lombok.Setter;

@Entity
@IdClass(LinkEntityId.class)
@Getter
@Setter
public class LinkEntity {
    @Id
    private Integer projectId;
    @Id
    private String url;
    private LinkStatus prodStatus = LinkStatus.NEW;
    private LinkStatus preProdStatus = LinkStatus.NEW;
    private String prodProcessedFlag = "N";
    private String preProdProcessedFlag = "N";

}
