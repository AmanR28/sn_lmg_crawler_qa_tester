package com.lmg.crawler_qa_tester.service;

public interface DomainService {
    // Generate Domain based on Project Info
    String getPreProdDomain();
    String getProdDomain();

}
