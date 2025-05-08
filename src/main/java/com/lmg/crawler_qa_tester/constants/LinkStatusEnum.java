package com.lmg.crawler_qa_tester.constants;

public enum LinkStatusEnum {
  NOT_PROCESSED("NOT_PROCESSED"),
  IN_PROGRESS("IN_PROGRESS"),
  FATAL("FATAL"),
  SUCCESS("SUCCESS"),
  NOT_FOUND("NOT_FOUND"),
  MISSING("MISSING");

  private final String value;

  LinkStatusEnum(String status) {
    this.value = status;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return this.value;
  }
}
