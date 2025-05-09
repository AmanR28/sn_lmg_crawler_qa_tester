package com.lmg.crawler_qa_tester.dto.comparator;

import lombok.Builder;

@Builder
public record ApiEntry(String name, String concept, String country, String sheetName) {}