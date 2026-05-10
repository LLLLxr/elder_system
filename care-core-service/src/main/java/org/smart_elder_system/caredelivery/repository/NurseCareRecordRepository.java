package org.smart_elder_system.caredelivery.repository;

import jakarta.persistence.LockModeType;
import org.smart_elder_system.caredelivery.po.NurseCareRecordPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NurseCareRecordRepository extends JpaRepository<NurseCareRecordPo, Long>, JpaSpecificationExecutor<NurseCareRecordPo> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM NurseCareRecordPo r WHERE r.id = :recordId")
    Optional<NurseCareRecordPo> findByIdForUpdate(@Param("recordId") Long recordId);
}
