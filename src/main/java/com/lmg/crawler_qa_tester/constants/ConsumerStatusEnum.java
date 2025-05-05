package com.lmg.crawler_qa_tester.constants;

public enum ConsumerStatusEnum {
  INIT("INIT"),
  RUNNING("RUNNING"),
  COMPLETED("COMPLETED"),
  STOPPED("STOPPED"),
  CANCELLED("CANCELLED");

  private final String value;

  ConsumerStatusEnum(String status) {
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
