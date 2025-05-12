package com.lmg.crawler_qa_tester.constants;

import lombok.Data;


public enum ReportStatus {
    NOT_AVAILABLE("N"),
    IN_PROGRESS("I"),
    SUCCESS("S"),
    ERROR("E");
    private final String code;

    ReportStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
