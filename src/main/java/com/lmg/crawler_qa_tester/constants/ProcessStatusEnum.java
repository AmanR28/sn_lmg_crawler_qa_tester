package com.lmg.crawler_qa_tester.constants;

public enum ProcessStatusEnum {
  INIT("INIT"),
  RUNNING("RUNNING"),
  COMPLETED("COMPLETED"),
  STOPPED("STOPPED"),
  CANCELLED("CANCELLED");

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
