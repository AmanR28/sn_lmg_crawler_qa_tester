package com.lmg.crawler_qa_tester.constants;

public enum ReportTypeEnum {
    PROD_LINKS_CSV("PROD_LINKS_CSV"),
    PROD_LINKS_EXCEL("PROD_LINKS_EXCEL"),
    PRE_PROD_LINKS_CSV("PRE_PROD_LINKS_CSV"),
    PRE_PROD_LINKS_EXCEL("PRE_PROD_LINKS_EXCEL"),
    COMPARE_LINKS_CSV("COMPARE_LINKS_CSV"),
    COMPARE_LINKS_EXCEL("COMPARE_LINKS_EXCEL");

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
