package org.smart_elder_system.careorchestration.repository;

import org.smart_elder_system.careorchestration.po.ServiceJourneyTransitionLogPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceJourneyTransitionLogRepository extends JpaRepository<ServiceJourneyTransitionLogPo, Long> {

    List<ServiceJourneyTransitionLogPo> findByApplicationIdOrderByTransitionTimeAscIdAsc(Long applicationId);

    List<ServiceJourneyTransitionLogPo> findByAgreementIdOrderByTransitionTimeAscIdAsc(Long agreementId);

    Optional<ServiceJourneyTransitionLogPo> findTopByApplicationIdAndJourneyEventOrderByTransitionTimeDescIdDesc(Long applicationId, String journeyEvent);
}
