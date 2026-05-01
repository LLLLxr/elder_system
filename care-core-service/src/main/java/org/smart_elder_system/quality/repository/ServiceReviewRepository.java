package org.smart_elder_system.quality.repository;

import jakarta.persistence.LockModeType;
import org.smart_elder_system.quality.po.ServiceReviewPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceReviewRepository extends JpaRepository<ServiceReviewPo, Long> {

    Optional<ServiceReviewPo> findTopByAgreementIdOrderByReviewedAtDescIdDesc(Long agreementId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM ServiceReviewPo r WHERE r.id = (SELECT MAX(r2.id) FROM ServiceReviewPo r2 WHERE r2.agreementId = :agreementId)")
    Optional<ServiceReviewPo> findLatestByAgreementIdForUpdate(@Param("agreementId") Long agreementId);

    @Query("SELECT COALESCE(AVG(r.satisfactionScore), 0) FROM ServiceReviewPo r")
    Double averageSatisfaction();

    @Query("SELECT FUNCTION('FORMATDATETIME', r.reviewedAt, 'yyyy-MM-dd'), COUNT(r) " +
            "FROM ServiceReviewPo r WHERE r.reviewedAt >= :startTime " +
            "GROUP BY FUNCTION('FORMATDATETIME', r.reviewedAt, 'yyyy-MM-dd') " +
            "ORDER BY FUNCTION('FORMATDATETIME', r.reviewedAt, 'yyyy-MM-dd')")
    List<Object[]> countReviewedDaily(@Param("startTime") LocalDateTime startTime);
}
