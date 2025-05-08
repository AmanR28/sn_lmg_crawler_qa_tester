package com.lmg.crawler_qa_tester.constants;

public enum ProcessStatusEnum {
  NEW("NEW"),
  RUNNING("RUNNING"),
  COMPLETED("COMPLETED"),
  FATAL("FATAL");

  private final String value;

  ProcessStatusEnum(String status) {
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
