package org.smart_elder_system.caredelivery.repository;

import jakarta.persistence.LockModeType;
import org.smart_elder_system.caredelivery.po.CaregiverCheckInRecordPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface CaregiverCheckInRecordRepository extends JpaRepository<CaregiverCheckInRecordPo, Long>, JpaSpecificationExecutor<CaregiverCheckInRecordPo> {

    Optional<CaregiverCheckInRecordPo> findTopByServicePlanIdAndCaregiverIdAndElderIdAndTaskDateOrderByIdDesc(
            Long servicePlanId,
            Long caregiverId,
            Long elderId,
            LocalDate taskDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM CaregiverCheckInRecordPo r WHERE r.servicePlanId = :servicePlanId AND r.caregiverId = :caregiverId AND r.elderId = :elderId AND r.taskDate = :taskDate")
    Optional<CaregiverCheckInRecordPo> findByUniqueKeyForUpdate(
            @Param("servicePlanId") Long servicePlanId,
            @Param("caregiverId") Long caregiverId,
            @Param("elderId") Long elderId,
            @Param("taskDate") LocalDate taskDate);
}
