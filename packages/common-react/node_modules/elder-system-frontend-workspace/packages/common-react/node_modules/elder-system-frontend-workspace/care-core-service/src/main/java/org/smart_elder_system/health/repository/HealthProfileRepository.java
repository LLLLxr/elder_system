package org.smart_elder_system.health.repository;

import jakarta.persistence.LockModeType;
import org.smart_elder_system.health.po.HealthProfilePo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HealthProfileRepository extends JpaRepository<HealthProfilePo, Long>, JpaSpecificationExecutor<HealthProfilePo> {

    Optional<HealthProfilePo> findTopByElderIdAndAgreementIdOrderByProfileDateDescIdDesc(Long elderId, Long agreementId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM HealthProfilePo p WHERE p.elderId = :elderId AND p.agreementId = :agreementId")
    Optional<HealthProfilePo> findByElderIdAndAgreementIdForUpdate(
            @Param("elderId") Long elderId,
            @Param("agreementId") Long agreementId);
}
