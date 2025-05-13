package com.lmg.crawler_qa_tester.service;

import com.lmg.crawler_qa_tester.repository.ReportRepository;
import com.lmg.crawler_qa_tester.repository.entity.ReportEntity;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
@Transactional
public class GetReportStatusService {
    @Autowired
    ReportRepository reportRepository;
    public Optional<ReportEntity> getReportStatus(Integer id)
    {
        return reportRepository.findById(id);

    }
}
