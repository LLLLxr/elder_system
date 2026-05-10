package org.smart_elder_system.admission.repository;

import jakarta.persistence.LockModeType;
import org.smart_elder_system.admission.po.FamilyVisitSlotPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyVisitSlotRepository extends JpaRepository<FamilyVisitSlotPo, Long> {

    List<FamilyVisitSlotPo> findBySlotDateAndStatusOrderByStartTimeAsc(LocalDate slotDate, String status);

    List<FamilyVisitSlotPo> findBySlotDateGreaterThanEqualAndStatusOrderBySlotDateAscStartTimeAsc(LocalDate slotDate, String status);

    List<FamilyVisitSlotPo> findBySlotDateBetween(LocalDate startDate, LocalDate endDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM FamilyVisitSlotPo s WHERE s.id = :slotId")
    Optional<FamilyVisitSlotPo> findByIdForUpdate(@Param("slotId") Long slotId);
}
