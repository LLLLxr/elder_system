package org.smart_elder_system.contract.repository;

import jakarta.persistence.LockModeType;
import org.smart_elder_system.contract.po.ServiceAgreementPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceAgreementRepository extends JpaRepository<ServiceAgreementPo, Long> {

    long countByStatus(String status);

    @Query("SELECT FUNCTION('FORMATDATETIME', a.effectiveDate, 'yyyy-MM-dd'), COUNT(a) " +
            "FROM ServiceAgreementPo a WHERE a.effectiveDate >= :startDate " +
            "GROUP BY FUNCTION('FORMATDATETIME', a.effectiveDate, 'yyyy-MM-dd') " +
            "ORDER BY FUNCTION('FORMATDATETIME', a.effectiveDate, 'yyyy-MM-dd')")
    List<Object[]> countEffectiveDaily(@Param("startDate") LocalDate startDate);

    Optional<ServiceAgreementPo> findTopByApplicationIdOrderByIdDesc(Long applicationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM ServiceAgreementPo a WHERE a.id = :agreementId")
    Optional<ServiceAgreementPo> findByIdForUpdate(@Param("agreementId") Long agreementId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM ServiceAgreementPo a WHERE a.id = (SELECT MAX(a2.id) FROM ServiceAgreementPo a2 WHERE a2.applicationId = :applicationId)")
    Optional<ServiceAgreementPo> findLatestByApplicationIdForUpdate(@Param("applicationId") Long applicationId);
}
