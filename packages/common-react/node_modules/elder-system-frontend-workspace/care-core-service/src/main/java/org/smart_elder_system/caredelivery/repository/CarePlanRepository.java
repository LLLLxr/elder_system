package org.smart_elder_system.caredelivery.repository;

import org.smart_elder_system.caredelivery.po.CarePlanPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarePlanRepository extends JpaRepository<CarePlanPo, Long> {

    long countByStatus(String status);

    Optional<CarePlanPo> findTopByAgreementIdOrderByPlanDateDescIdDesc(Long agreementId);
}
