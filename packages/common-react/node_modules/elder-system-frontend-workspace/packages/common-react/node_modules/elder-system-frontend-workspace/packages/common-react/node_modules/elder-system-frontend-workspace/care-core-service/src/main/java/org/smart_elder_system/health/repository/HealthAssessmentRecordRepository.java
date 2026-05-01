package org.smart_elder_system.health.repository;

import org.smart_elder_system.health.po.HealthAssessmentRecordPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

@Repository
public interface HealthAssessmentRecordRepository extends JpaRepository<HealthAssessmentRecordPo, Long>, JpaSpecificationExecutor<HealthAssessmentRecordPo> {

    Optional<HealthAssessmentRecordPo> findTopByElderIdAndAssessmentTypeInOrderByAssessedAtDescIdDesc(
            Long elderId,
            Collection<String> assessmentTypes);

    Optional<HealthAssessmentRecordPo> findTopByElderIdAndAssessmentTypeInAndAssessedAtGreaterThanEqualOrderByAssessedAtDescIdDesc(
            Long elderId,
            Collection<String> assessmentTypes,
            LocalDateTime assessedAt);

    Optional<HealthAssessmentRecordPo> findTopByApplicationIdAndAssessmentTypeInOrderByAssessedAtDescIdDesc(
            Long applicationId,
            Collection<String> assessmentTypes);

    Optional<HealthAssessmentRecordPo> findTopByAgreementIdAndAssessmentTypeOrderByAssessedAtDescIdDesc(
            Long agreementId,
            String assessmentType);

    List<HealthAssessmentRecordPo> findByApplicationIdOrderByAssessedAtDescIdDesc(Long applicationId);
}
