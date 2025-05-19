package com.lmg.crawler_qa_tester.constants;

public enum PageTypeEnum {
  DEPARTMENT("DEPARTMENT"),
  CATEGORY("CATEGORY"),
  PRODUCT("PRODUCT"),
  SEARCH("SEARCH"),
  OTHER("OTHER");

  private final String value;

  PageTypeEnum(String type) {
    this.value = type;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return this.value;
  }
}
