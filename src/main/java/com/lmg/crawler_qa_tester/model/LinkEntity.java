package com.lmg.crawler_qa_tester.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LinkEntity {
    private int id;
    private String url;
    private String processed;
    private String status;
    private String type;

    public String toString() {
        return "LinkEntity [id=" + id + ", url=" + url + ", processed=" + processed + ", status=" + status
                + ", type=" + type + "]";
    }
}
