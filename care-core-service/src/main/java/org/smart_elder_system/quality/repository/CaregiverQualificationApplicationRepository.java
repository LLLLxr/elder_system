package org.smart_elder_system.quality.repository;

import jakarta.persistence.LockModeType;
import org.smart_elder_system.quality.po.CaregiverQualificationApplicationPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaregiverQualificationApplicationRepository extends JpaRepository<CaregiverQualificationApplicationPo, Long> {

    List<CaregiverQualificationApplicationPo> findByCaregiverUserIdOrderByCreatedDateTimeUtcDesc(Long caregiverUserId);

    Optional<CaregiverQualificationApplicationPo> findTopByCaregiverUserIdOrderByCreatedDateTimeUtcDescIdDesc(Long caregiverUserId);

    List<CaregiverQualificationApplicationPo> findByStatusOrderByCreatedDateTimeUtcDesc(String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM CaregiverQualificationApplicationPo a WHERE a.id = :applicationId")
    Optional<CaregiverQualificationApplicationPo> findByIdForUpdate(@Param("applicationId") Long applicationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM CaregiverQualificationApplicationPo a WHERE a.caregiverUserId = :caregiverUserId ORDER BY a.createdDateTimeUtc DESC, a.id DESC")
    List<CaregiverQualificationApplicationPo> findByCaregiverUserIdForUpdate(@Param("caregiverUserId") Long caregiverUserId);
}
