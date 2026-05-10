package org.smart_elder_system.admission.repository;

import jakarta.persistence.LockModeType;
import org.smart_elder_system.admission.po.FamilyVisitReservationPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyVisitReservationRepository extends JpaRepository<FamilyVisitReservationPo, Long> {

    List<FamilyVisitReservationPo> findByFamilyUserIdOrderByCreatedDateTimeUtcDesc(Long familyUserId);

    List<FamilyVisitReservationPo> findByStatusOrderByCreatedDateTimeUtcDesc(String status);

    boolean existsBySlotIdAndFamilyUserIdAndElderId(Long slotId, Long familyUserId, Long elderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM FamilyVisitReservationPo r WHERE r.id = :reservationId")
    Optional<FamilyVisitReservationPo> findByIdForUpdate(@Param("reservationId") Long reservationId);
}
