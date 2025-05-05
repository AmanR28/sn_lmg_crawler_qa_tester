package com.lmg.crawler_qa_tester.constants;

public enum EnvironmentEnum {
  PROD("PROD"),
  PRE_PROD("PRE_PROD");

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
