package com.lmg.crawler_qa_tester.constants;

import lombok.Getter;

@Getter
public enum ProcessStatusEnum {
  NEW("NEW"),
  RUNNING("RUNNING"),
  POST_RUNNING("POST_RUNNING"),
  COMPLETED("COMPLETED"),
  FATAL("FATAL");

  private final String value;

  ProcessStatusEnum(String status) {
    this.value = status;
  }

    @Override
  public String toString() {
    return this.value;
  }
}
