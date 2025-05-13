package com.lmg.crawler_qa_tester.constants;

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

    public static ReportStatus fromCode(String code) {
        for (ReportStatus status : ReportStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
