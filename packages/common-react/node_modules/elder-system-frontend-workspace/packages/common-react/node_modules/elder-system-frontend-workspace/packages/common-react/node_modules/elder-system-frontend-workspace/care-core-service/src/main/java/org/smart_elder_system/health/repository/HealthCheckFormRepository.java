package org.smart_elder_system.health.repository;

import org.smart_elder_system.health.po.HealthCheckFormPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HealthCheckFormRepository extends JpaRepository<HealthCheckFormPo, Long>, JpaSpecificationExecutor<HealthCheckFormPo> {

    Optional<HealthCheckFormPo> findTopByElderIdAndAgreementIdOrderByCheckDateDescIdDesc(Long elderId, Long agreementId);

    Optional<HealthCheckFormPo> findTopByElderIdOrderByCheckDateDescIdDesc(Long elderId);

    Optional<HealthCheckFormPo> findTopByElderIdAndAuthorUserIdOrderByCheckDateDescIdDesc(Long elderId, Long authorUserId);

    List<HealthCheckFormPo> findByElderIdAndAgreementIdOrderByCheckDateDescIdDesc(Long elderId, Long agreementId);

    List<HealthCheckFormPo> findByElderIdOrderByCheckDateDescIdDesc(Long elderId);

    List<HealthCheckFormPo> findByAuthorUserIdOrderByCheckDateDescIdDesc(Long authorUserId);

    List<HealthCheckFormPo> findByElderIdAndAuthorUserIdOrderByCheckDateDescIdDesc(Long elderId, Long authorUserId);

}
