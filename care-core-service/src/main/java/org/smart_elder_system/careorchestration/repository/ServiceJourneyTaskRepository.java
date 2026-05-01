package org.smart_elder_system.careorchestration.repository;

import jakarta.persistence.LockModeType;
import org.smart_elder_system.careorchestration.po.ServiceJourneyTaskPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceJourneyTaskRepository extends JpaRepository<ServiceJourneyTaskPo, Long> {

    Optional<ServiceJourneyTaskPo> findTopByApplicationIdAndTaskTypeAndStatusOrderByCreatedDateTimeUtcDesc(
            Long applicationId,
            String taskType,
            String status);

    Optional<ServiceJourneyTaskPo> findTopByApplicationIdAndTaskTypeAndStatusInOrderByCreatedDateTimeUtcDesc(
            Long applicationId,
            String taskType,
            Collection<String> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM ServiceJourneyTaskPo t WHERE t.id = (SELECT MAX(t2.id) FROM ServiceJourneyTaskPo t2 WHERE t2.applicationId = :applicationId AND t2.taskType = :taskType AND t2.status IN :statuses)")
    Optional<ServiceJourneyTaskPo> findLatestOpenTaskForUpdate(
            @Param("applicationId") Long applicationId,
            @Param("taskType") String taskType,
            @Param("statuses") Collection<String> statuses);

    List<ServiceJourneyTaskPo> findByStatusAndDueAtBefore(String status, LocalDateTime dueAt);

    List<ServiceJourneyTaskPo> findByApplicationIdOrderByCreatedDateTimeUtcAsc(Long applicationId);

    List<ServiceJourneyTaskPo> findByApplicationIdOrderByCreatedDateTimeUtcDesc(Long applicationId);

    List<ServiceJourneyTaskPo> findByElderIdOrderByCreatedDateTimeUtcDesc(Long elderId);

    List<ServiceJourneyTaskPo> findByAgreementIdOrderByCreatedDateTimeUtcDesc(Long agreementId);

    List<ServiceJourneyTaskPo> findByTaskTypeOrderByCreatedDateTimeUtcDesc(String taskType);

    List<ServiceJourneyTaskPo> findByStatusOrderByCreatedDateTimeUtcDesc(String status);

    List<ServiceJourneyTaskPo> findByAssigneeRoleOrderByCreatedDateTimeUtcDesc(String assigneeRole);

    @Query(value = "SELECT t FROM ServiceJourneyTaskPo t " +
            "WHERE (:applicationId IS NULL OR t.applicationId = :applicationId) " +
            "AND (:elderId IS NULL OR t.elderId = :elderId) " +
            "AND (:agreementId IS NULL OR t.agreementId = :agreementId) " +
            "AND (:taskType IS NULL OR t.taskType = :taskType) " +
            "AND (:assigneeRole IS NULL OR t.assigneeRole = :assigneeRole) " +
            "AND (:statuses IS NULL OR t.status IN :statuses)",
            countQuery = "SELECT COUNT(t) FROM ServiceJourneyTaskPo t " +
                    "WHERE (:applicationId IS NULL OR t.applicationId = :applicationId) " +
                    "AND (:elderId IS NULL OR t.elderId = :elderId) " +
                    "AND (:agreementId IS NULL OR t.agreementId = :agreementId) " +
                    "AND (:taskType IS NULL OR t.taskType = :taskType) " +
                    "AND (:assigneeRole IS NULL OR t.assigneeRole = :assigneeRole) " +
                    "AND (:statuses IS NULL OR t.status IN :statuses)")
    Page<ServiceJourneyTaskPo> searchTasks(
            @Param("applicationId") Long applicationId,
            @Param("elderId") Long elderId,
            @Param("agreementId") Long agreementId,
            @Param("taskType") String taskType,
            @Param("assigneeRole") String assigneeRole,
            @Param("statuses") Collection<String> statuses,
            Pageable pageable);

    @Query("SELECT t.taskType, COUNT(t) FROM ServiceJourneyTaskPo t " +
            "WHERE (:applicationId IS NULL OR t.applicationId = :applicationId) " +
            "AND (:elderId IS NULL OR t.elderId = :elderId) " +
            "AND (:agreementId IS NULL OR t.agreementId = :agreementId) " +
            "AND (:taskType IS NULL OR t.taskType = :taskType) " +
            "AND (:assigneeRole IS NULL OR t.assigneeRole = :assigneeRole) " +
            "AND (:statuses IS NULL OR t.status IN :statuses) " +
            "GROUP BY t.taskType ORDER BY t.taskType")
    List<Object[]> countByTaskType(
            @Param("applicationId") Long applicationId,
            @Param("elderId") Long elderId,
            @Param("agreementId") Long agreementId,
            @Param("taskType") String taskType,
            @Param("assigneeRole") String assigneeRole,
            @Param("statuses") Collection<String> statuses);

    @Query("SELECT t.status, COUNT(t) FROM ServiceJourneyTaskPo t " +
            "WHERE (:applicationId IS NULL OR t.applicationId = :applicationId) " +
            "AND (:elderId IS NULL OR t.elderId = :elderId) " +
            "AND (:agreementId IS NULL OR t.agreementId = :agreementId) " +
            "AND (:taskType IS NULL OR t.taskType = :taskType) " +
            "AND (:assigneeRole IS NULL OR t.assigneeRole = :assigneeRole) " +
            "AND (:statuses IS NULL OR t.status IN :statuses) " +
            "GROUP BY t.status ORDER BY t.status")
    List<Object[]> countByStatus(
            @Param("applicationId") Long applicationId,
            @Param("elderId") Long elderId,
            @Param("agreementId") Long agreementId,
            @Param("taskType") String taskType,
            @Param("assigneeRole") String assigneeRole,
            @Param("statuses") Collection<String> statuses);
}
