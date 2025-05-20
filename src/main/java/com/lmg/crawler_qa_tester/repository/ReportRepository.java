package com.lmg.crawler_qa_tester.repository;
import com.lmg.crawler_qa_tester.repository.entity.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface ReportRepository extends JpaRepository<ReportEntity,Integer> {
    @Query("SELECT r FROM ReportEntity r WHERE r.host = :domain AND r.country = :country AND r.locale = :locale AND r.department = :department")
    Optional<ReportEntity> findExistingReport( @Param("domain") String domain,
                                               @Param("country") String country,
                                               @Param("locale") String locale,
                                               @Param("department") String department);
    @Query("SELECT r FROM ReportEntity r WHERE r.crawlId = :id")
    Optional<ReportEntity> findByCrawlId(@Param("id") Integer id);
    Optional<ReportEntity> findById(Integer id);
    long countByIdAndStatus(Long id, String status);
    @Modifying
    @Query("UPDATE ReportEntity r SET r.status = :status, r.updatedAt = CURRENT_TIMESTAMP WHERE r.id = :id")
    int updateStatusAndTimeById(@Param("id") Long id, @Param("status") String status);

}
