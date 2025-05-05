package com.lmg.crawler_qa_tester.constants;

public enum ReportTypeEnum {
    COMPARE_LINKS_CSV("COMPARE_LINKS_CSV"),
    PROD_CATEGORY_CSV("PROD_CATEGORY_CSV"),
    PRE_PROD_CATEGORY_CSV("PRE_PROD_CATEGORY_CSV");

    private final String value;

    ReportTypeEnum(String reportType) {
        this.value = reportType;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
