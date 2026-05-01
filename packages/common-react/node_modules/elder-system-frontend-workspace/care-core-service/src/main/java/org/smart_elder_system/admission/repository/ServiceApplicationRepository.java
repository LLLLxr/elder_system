package org.smart_elder_system.admission.repository;

import jakarta.persistence.LockModeType;
import org.smart_elder_system.admission.po.ServiceApplicationPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceApplicationRepository extends JpaRepository<ServiceApplicationPo, Long> {

    long countByStatus(String status);

    List<ServiceApplicationPo> findByStatusOrderBySubmittedAtAsc(String status);

    List<ServiceApplicationPo> findByStatusInOrderBySubmittedAtDesc(Collection<String> statuses);

    List<ServiceApplicationPo> findByElderIdOrderBySubmittedAtDesc(Long elderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM ServiceApplicationPo a WHERE a.elderId = :elderId ORDER BY a.submittedAt DESC, a.id DESC")
    List<ServiceApplicationPo> findByElderIdForUpdate(@Param("elderId") Long elderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM ServiceApplicationPo a WHERE a.id = :applicationId")
    Optional<ServiceApplicationPo> findByIdForUpdate(@Param("applicationId") Long applicationId);

    List<ServiceApplicationPo> findByApplicantNameOrderBySubmittedAtDesc(String applicantName);

    Optional<ServiceApplicationPo> findTopByApplicantNameOrderBySubmittedAtDescIdDesc(String applicantName);

    @Query("SELECT FUNCTION('FORMATDATETIME', a.submittedAt, 'yyyy-MM-dd'), COUNT(a) " +
            "FROM ServiceApplicationPo a WHERE a.submittedAt >= :startTime " +
            "GROUP BY FUNCTION('FORMATDATETIME', a.submittedAt, 'yyyy-MM-dd') " +
            "ORDER BY FUNCTION('FORMATDATETIME', a.submittedAt, 'yyyy-MM-dd')")
    List<Object[]> countSubmittedDaily(@Param("startTime") LocalDateTime startTime);
}
