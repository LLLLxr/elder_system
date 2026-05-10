package org.smart_elder_system.careorchestration.service;

import lombok.RequiredArgsConstructor;
import org.smart_elder_system.careorchestration.dto.CareAnalyticsOverviewDto;
import org.smart_elder_system.careorchestration.dto.ServiceJourneyTaskItemDto;
import org.smart_elder_system.careorchestration.dto.ServiceJourneyTaskOverviewDto;
import org.smart_elder_system.careorchestration.journey.ServiceJourneyState;
import org.smart_elder_system.careorchestration.po.ServiceJourneyTaskPo;
import org.smart_elder_system.careorchestration.repository.ServiceJourneyTaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ServiceJourneyTaskService {

    public static final String TASK_TYPE_ADMISSION_ASSESSMENT = "ADMISSION_ASSESSMENT";
    public static final String TASK_TYPE_HEALTH_ASSESSMENT = "HEALTH_ASSESSMENT";
    public static final String TASK_TYPE_FAMILY_VISIT_REVIEW = "FAMILY_VISIT_REVIEW";
    public static final String TASK_TYPE_CAREGIVER_QUALIFICATION_REVIEW = "CAREGIVER_QUALIFICATION_REVIEW";
    public static final String TASK_TYPE_DAILY_CARE_TASK_EXECUTION = "DAILY_CARE_TASK_EXECUTION";
    public static final String TASK_TYPE_NURSE_CARE_RECORD = "NURSE_CARE_RECORD";
    public static final String TASK_TYPE_DOCTOR_ROUND_RECORD = "DOCTOR_ROUND_RECORD";
    private static final String ROLE_CUSTOMER_SERVICE = "CUSTOMER_SERVICE";
    private static final String ROLE_MEDICAL_STAFF = "MEDICAL_STAFF";
    private static final long ADMISSION_TASK_HOURS = 24L;
    private static final long HEALTH_TASK_HOURS = 24L;
    private static final long FAMILY_VISIT_REVIEW_TASK_HOURS = 24L;
    private static final long CAREGIVER_QUALIFICATION_REVIEW_TASK_HOURS = 24L;
    private static final long DAILY_CARE_TASK_HOURS = 24L;
    private static final long NURSE_CARE_RECORD_TASK_HOURS = 24L;
    private static final long DOCTOR_ROUND_RECORD_TASK_HOURS = 24L;

    private final ServiceJourneyTaskRepository serviceJourneyTaskRepository;

    @Transactional
    public void createAdmissionAssessmentTask(Long applicationId, Long elderId) {
        createTask(applicationId, null, elderId, TASK_TYPE_ADMISSION_ASSESSMENT,
                ServiceJourneyState.PENDING_ASSESSMENT.name(), ROLE_CUSTOMER_SERVICE, LocalDateTime.now().plusHours(ADMISSION_TASK_HOURS));
    }

    @Transactional
    public void createHealthAssessmentTask(Long applicationId, Long elderId) {
        createTask(applicationId, null, elderId, TASK_TYPE_HEALTH_ASSESSMENT,
                ServiceJourneyState.PENDING_HEALTH_ASSESSMENT.name(), ROLE_CUSTOMER_SERVICE, LocalDateTime.now().plusHours(HEALTH_TASK_HOURS));
    }

    @Transactional
    public void createFamilyVisitReviewTask(Long reservationId, Long elderId) {
        createTask(reservationId, null, elderId, TASK_TYPE_FAMILY_VISIT_REVIEW,
                "FAMILY_VISIT_PENDING_REVIEW", ROLE_MEDICAL_STAFF, LocalDateTime.now().plusHours(FAMILY_VISIT_REVIEW_TASK_HOURS));
    }

    @Transactional
    public void createCaregiverQualificationReviewTask(Long applicationId, Long caregiverUserId) {
        createTask(applicationId, null, caregiverUserId, TASK_TYPE_CAREGIVER_QUALIFICATION_REVIEW,
                "CAREGIVER_QUALIFICATION_PENDING_REVIEW", ROLE_MEDICAL_STAFF, LocalDateTime.now().plusHours(CAREGIVER_QUALIFICATION_REVIEW_TASK_HOURS));
    }

    @Transactional
    public void createDailyCareTask(Long applicationId, Long agreementId, Long elderId) {
        createTask(applicationId, agreementId, elderId, TASK_TYPE_DAILY_CARE_TASK_EXECUTION,
                "DAILY_CARE_TASK_PENDING", ROLE_MEDICAL_STAFF, LocalDateTime.now().plusHours(DAILY_CARE_TASK_HOURS));
    }

    @Transactional
    public void createNurseCareRecordTask(Long applicationId, Long agreementId, Long elderId) {
        createTask(applicationId, agreementId, elderId, TASK_TYPE_NURSE_CARE_RECORD,
                "NURSE_CARE_RECORD_PENDING", ROLE_MEDICAL_STAFF, LocalDateTime.now().plusHours(NURSE_CARE_RECORD_TASK_HOURS));
    }

    @Transactional
    public void createDoctorRoundRecordTask(Long applicationId, Long agreementId, Long elderId) {
        createTask(applicationId, agreementId, elderId, TASK_TYPE_DOCTOR_ROUND_RECORD,
                "DOCTOR_ROUND_RECORD_PENDING", ROLE_MEDICAL_STAFF, LocalDateTime.now().plusHours(DOCTOR_ROUND_RECORD_TASK_HOURS));
    }

    @Transactional(readOnly = true)
    public Page<ServiceJourneyTaskItemDto> listTasks(
            Long applicationId,
            Long elderId,
            Long agreementId,
            String taskType,
            List<String> statuses,
            String assigneeRole,
            int page,
            int size,
            String sortBy,
            String sortOrder) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.max(size, 1),
                buildSort(sortBy, sortOrder));
        return serviceJourneyTaskRepository.searchTasks(
                        applicationId,
                        elderId,
                        agreementId,
                        taskType,
                        assigneeRole,
                        normalizeStatuses(statuses),
                        pageable)
                .map(this::toItemDto);
    }

    @Transactional(readOnly = true)
    public List<ServiceJourneyTaskItemDto> listTaskTimeline(Long applicationId) {
        return serviceJourneyTaskRepository.findByApplicationIdOrderByCreatedDateTimeUtcAsc(applicationId).stream()
                .map(this::toItemDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServiceJourneyTaskOverviewDto getTaskOverview(
            Long applicationId,
            Long elderId,
            Long agreementId,
            String taskType,
            List<String> statuses,
            String assigneeRole) {
        Collection<String> normalizedStatuses = normalizeStatuses(statuses);
        List<Object[]> statusRows = serviceJourneyTaskRepository.countByStatus(
                applicationId,
                elderId,
                agreementId,
                taskType,
                assigneeRole,
                normalizedStatuses);
        List<Object[]> taskTypeRows = serviceJourneyTaskRepository.countByTaskType(
                applicationId,
                elderId,
                agreementId,
                taskType,
                assigneeRole,
                normalizedStatuses);

        ServiceJourneyTaskOverviewDto dto = new ServiceJourneyTaskOverviewDto();
        dto.setPendingCount(resolveCount(statusRows, ServiceJourneyTaskPo.STATUS_PENDING));
        dto.setOverdueCount(resolveCount(statusRows, ServiceJourneyTaskPo.STATUS_OVERDUE));
        dto.setCompletedCount(resolveCount(statusRows, ServiceJourneyTaskPo.STATUS_COMPLETED));
        dto.setCancelledCount(resolveCount(statusRows, ServiceJourneyTaskPo.STATUS_CANCELLED));
        dto.setTaskTypeDistribution(toStagePoints(taskTypeRows));
        dto.setStatusDistribution(toStagePoints(statusRows));
        return dto;
    }

    @Transactional
    public void completeOpenTask(Long applicationId, String taskType) {
        updateLatestOpenTask(applicationId, taskType, ServiceJourneyTaskPo.STATUS_COMPLETED);
    }

    @Transactional
    public void cancelOpenTask(Long applicationId, String taskType) {
        updateLatestOpenTask(applicationId, taskType, ServiceJourneyTaskPo.STATUS_CANCELLED);
    }

    @Transactional
    public int markOverdueTasks(LocalDateTime now) {
        List<ServiceJourneyTaskPo> tasks = serviceJourneyTaskRepository.findByStatusAndDueAtBefore(ServiceJourneyTaskPo.STATUS_PENDING, now);
        for (ServiceJourneyTaskPo task : tasks) {
            task.setStatus(ServiceJourneyTaskPo.STATUS_OVERDUE);
        }
        serviceJourneyTaskRepository.saveAll(tasks);
        return tasks.size();
    }

    private void updateLatestOpenTask(Long applicationId, String taskType, String targetStatus) {
        serviceJourneyTaskRepository
                .findLatestOpenTaskForUpdate(
                        applicationId,
                        taskType,
                        Set.of(ServiceJourneyTaskPo.STATUS_PENDING, ServiceJourneyTaskPo.STATUS_OVERDUE))
                .ifPresent(task -> {
                    task.setStatus(targetStatus);
                    task.setOpenFlag(null);
                    task.setCompletedAt(LocalDateTime.now());
                    serviceJourneyTaskRepository.save(task);
                });
    }

    private void createTask(
            Long applicationId,
            Long agreementId,
            Long elderId,
            String taskType,
            String currentState,
            String assigneeRole,
            LocalDateTime dueAt) {
        Optional<ServiceJourneyTaskPo> existingOpenTask = serviceJourneyTaskRepository.findLatestOpenTaskForUpdate(
                applicationId,
                taskType,
                Set.of(ServiceJourneyTaskPo.STATUS_PENDING, ServiceJourneyTaskPo.STATUS_OVERDUE));
        if (existingOpenTask.isPresent()) {
            ServiceJourneyTaskPo task = existingOpenTask.get();
            if (currentState.equals(task.getCurrentState())
                    && java.util.Objects.equals(agreementId, task.getAgreementId())
                    && java.util.Objects.equals(elderId, task.getElderId())
                    && java.util.Objects.equals(assigneeRole, task.getAssigneeRole())) {
                task.setDueAt(dueAt);
                task.setStatus(ServiceJourneyTaskPo.STATUS_PENDING);
                task.setOpenFlag(1);
                task.setCompletedAt(null);
                serviceJourneyTaskRepository.save(task);
                return;
            }

            task.setStatus(ServiceJourneyTaskPo.STATUS_CANCELLED);
            task.setOpenFlag(null);
            task.setCompletedAt(LocalDateTime.now());
            serviceJourneyTaskRepository.save(task);
        }

        ServiceJourneyTaskPo task = new ServiceJourneyTaskPo();
        task.setApplicationId(applicationId);
        task.setAgreementId(agreementId);
        task.setElderId(elderId);
        task.setTaskType(taskType);
        task.setCurrentState(currentState);
        task.setAssigneeRole(assigneeRole);
        task.setStatus(ServiceJourneyTaskPo.STATUS_PENDING);
        task.setOpenFlag(1);
        task.setDueAt(dueAt);
        serviceJourneyTaskRepository.save(task);
    }

    private ServiceJourneyTaskItemDto toItemDto(ServiceJourneyTaskPo po) {
        ServiceJourneyTaskItemDto dto = new ServiceJourneyTaskItemDto();
        dto.setTaskId(po.getId());
        dto.setApplicationId(po.getApplicationId());
        dto.setAgreementId(po.getAgreementId());
        dto.setElderId(po.getElderId());
        dto.setTaskType(po.getTaskType());
        dto.setCurrentState(po.getCurrentState());
        dto.setAssigneeRole(po.getAssigneeRole());
        dto.setStatus(po.getStatus());
        dto.setDueAt(po.getDueAt());
        dto.setCompletedAt(po.getCompletedAt());
        dto.setCreatedAt(po.getCreatedDateTimeUtc());
        return dto;
    }

    private List<CareAnalyticsOverviewDto.StagePoint> toStagePoints(List<Object[]> rows) {
        return rows.stream().map(row -> {
            CareAnalyticsOverviewDto.StagePoint point = new CareAnalyticsOverviewDto.StagePoint();
            point.setName(String.valueOf(row[0]));
            point.setValue(((Number) row[1]).intValue());
            return point;
        }).toList();
    }

    private Collection<String> normalizeStatuses(List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return null;
        }
        return statuses.stream()
                .filter(status -> status != null && !status.isBlank())
                .toList();
    }

    private Sort buildSort(String sortBy, String sortOrder) {
        String actualSortBy = switch (sortBy) {
            case "createdAt" -> "createdDateTimeUtc";
            case "status", "taskType", "dueAt", "completedAt", "createdDateTimeUtc" -> sortBy;
            default -> "dueAt";
        };
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, actualSortBy).and(Sort.by(Sort.Direction.DESC, "id"));
    }

    private int resolveCount(List<Object[]> rows, String status) {
        return rows.stream()
                .filter(row -> status.equals(String.valueOf(row[0])))
                .map(row -> ((Number) row[1]).intValue())
                .findFirst()
                .orElse(0);
    }
}
