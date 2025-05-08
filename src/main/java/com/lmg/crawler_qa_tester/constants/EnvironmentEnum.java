package com.lmg.crawler_qa_tester.constants;

public enum EnvironmentEnum {
  FROM_ENV("FROM_ENV"),
  TO_ENV("TO_ENV");

  private final String value;

  EnvironmentEnum(String env) {
    this.value = env;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return this.value;
  }
}
