package org.smart_elder_system.health.repository;

import jakarta.persistence.LockModeType;
import org.smart_elder_system.health.po.DoctorRoundRecordPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorRoundRecordRepository extends JpaRepository<DoctorRoundRecordPo, Long>, JpaSpecificationExecutor<DoctorRoundRecordPo> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM DoctorRoundRecordPo r WHERE r.id = :recordId")
    Optional<DoctorRoundRecordPo> findByIdForUpdate(@Param("recordId") Long recordId);
}
