package org.smart_elder_system.caredelivery.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.smart_elder_system.caredelivery.CareDeliveryAuthorizationException;
import org.smart_elder_system.caredelivery.CareDeliveryAuthorizationPolicy;
import org.smart_elder_system.caredelivery.vo.CarePlan;
import org.smart_elder_system.caredelivery.vo.CaregiverCheckInRecord;
import org.smart_elder_system.caredelivery.vo.NurseCareRecord;
import org.smart_elder_system.caredelivery.po.CarePlanPo;
import org.smart_elder_system.caredelivery.po.CaregiverCheckInRecordPo;
import org.smart_elder_system.caredelivery.po.NurseCareRecordPo;
import org.smart_elder_system.caredelivery.repository.CarePlanRepository;
import org.smart_elder_system.caredelivery.repository.CaregiverCheckInRecordRepository;
import org.smart_elder_system.caredelivery.repository.NurseCareRecordRepository;
import org.smart_elder_system.careorchestration.service.ServiceJourneyTaskService;
import org.smart_elder_system.careorchestration.service.ServiceJourneyTransitionLogService;
import org.smart_elder_system.common.dto.caredelivery.CarePlanDto;
import org.smart_elder_system.common.dto.caredelivery.CaregiverCheckInRecordDto;
import org.smart_elder_system.common.dto.caredelivery.CaregiverCheckInSubmitDto;
import org.smart_elder_system.common.dto.caredelivery.DailyCareTaskDto;
import org.smart_elder_system.common.dto.caredelivery.DailyCareTaskItemDto;
import org.smart_elder_system.common.dto.caredelivery.FamilyServicePlanDto;
import org.smart_elder_system.common.dto.caredelivery.NurseCareRecordDto;
import org.smart_elder_system.common.dto.caredelivery.NurseCareRecordSaveDto;
import org.smart_elder_system.contract.po.ServiceAgreementPo;
import org.smart_elder_system.contract.repository.ServiceAgreementRepository;
import org.smart_elder_system.health.repository.HealthCheckFormRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CareDeliveryService {

    private static final List<String> ACTIVE_PLAN_STATUSES = List.of("ACTIVE", CarePlan.STATUS_CREATED, CarePlan.STATUS_IN_PROGRESS);
    private static final String BUSINESS_TYPE_CHECK_IN = "CAREGIVER_CHECK_IN_RECORD";
    private static final String BUSINESS_TYPE_NURSE_RECORD = "NURSE_CARE_RECORD";
    private static final String ACTION_CHECK_IN = "CARE_TASK_CHECKED_IN";
    private static final String ACTION_NURSE_RECORD_CREATED = "NURSE_CARE_RECORD_CREATED";
    private static final String ACTION_NURSE_RECORD_UPDATED = "NURSE_CARE_RECORD_UPDATED";

    private final CarePlanRepository carePlanRepository;
    private final CaregiverCheckInRecordRepository caregiverCheckInRecordRepository;
    private final NurseCareRecordRepository nurseCareRecordRepository;
    private final ServiceAgreementRepository serviceAgreementRepository;
    private final HealthCheckFormRepository healthCheckFormRepository;
    private final CareDeliveryAuthorizationPolicy careDeliveryAuthorizationPolicy;
    private final ServiceJourneyTaskService serviceJourneyTaskService;
    private final ServiceJourneyTransitionLogService serviceJourneyTransitionLogService;
    private final ObjectMapper objectMapper;

    public String getModuleScope() {
        return "护理执行模块：负责个性化护理计划、服务任务派发与执行记录";
    }

    @Transactional(rollbackFor = Exception.class)
    public CarePlanDto createCarePlan(CarePlanDto carePlanDto) {
        CarePlan domain = CarePlan.fromDto(carePlanDto);
        domain.create();

        CarePlanPo saved = carePlanRepository.save(domain.toPo(objectMapper));
        domain.setPlanId(saved.getId());
        createPhaseThreeTasks(saved);
        return domain.toDto();
    }

    @Transactional(rollbackFor = Exception.class)
    public CarePlanDto startCarePlan(Long planId) {
        CarePlanPo po = carePlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("未找到护理计划"));

        CarePlan domain = CarePlan.fromPo(po, objectMapper);
        domain.start();

        domain.applyTo(po, objectMapper);
        carePlanRepository.save(po);
        createPhaseThreeTasks(po);
        return domain.toDto();
    }

    @Transactional(rollbackFor = Exception.class)
    public CarePlanDto closeCarePlan(Long planId) {
        CarePlanPo po = carePlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("未找到护理计划"));

        CarePlan domain = CarePlan.fromPo(po, objectMapper);
        domain.close();

        domain.applyTo(po, objectMapper);
        carePlanRepository.save(po);
        return domain.toDto();
    }

    @Transactional(readOnly = true)
    public List<DailyCareTaskDto> listMyDailyTasks(LocalDate taskDate, Long elderId) {
        careDeliveryAuthorizationPolicy.requireDailyTaskListPermission();
        Long caregiverId = careDeliveryAuthorizationPolicy.requireCurrentUserId();

        List<CarePlanPo> plans = carePlanRepository.findByAssignedCaregiverIdAndStatusInOrderByPlanDateDescIdDesc(caregiverId, ACTIVE_PLAN_STATUSES);
        return plans.stream()
                .filter(plan -> elderId == null || elderId.equals(plan.getElderId()))
                .map(plan -> buildDailyTask(plan, caregiverId, taskDate))
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public CaregiverCheckInRecordDto submitCheckIn(Long servicePlanId, CaregiverCheckInSubmitDto dto) {
        careDeliveryAuthorizationPolicy.requireDailyTaskCheckInPermission();
        Long caregiverId = careDeliveryAuthorizationPolicy.requireCurrentUserId();
        String caregiverName = careDeliveryAuthorizationPolicy.requireCurrentUsername();

        CarePlanPo plan = carePlanRepository.findById(servicePlanId)
                .orElseThrow(() -> new IllegalArgumentException("未找到服务计划"));
        ensurePlanAssignedToCurrentCaregiver(plan, caregiverId);
        if (!plan.getElderId().equals(dto.getElderId())) {
            throw new IllegalArgumentException("打卡老人信息与服务计划不一致");
        }

        CaregiverCheckInRecordPo po = caregiverCheckInRecordRepository
                .findByUniqueKeyForUpdate(servicePlanId, caregiverId, dto.getElderId(), dto.getTaskDate())
                .orElseGet(CaregiverCheckInRecordPo::new);

        CaregiverCheckInRecord domain = po.getId() == null
                ? CaregiverCheckInRecord.create(servicePlanId, caregiverId, caregiverName, resolveElderName(dto.getElderId()), dto)
                : CaregiverCheckInRecord.fromPo(po, objectMapper);
        if (po.getId() != null) {
            domain.resubmit(dto.getTaskItems());
        }

        domain.applyTo(po, objectMapper);
        CaregiverCheckInRecordPo saved = caregiverCheckInRecordRepository.save(po);
        CaregiverCheckInRecord savedDomain = CaregiverCheckInRecord.fromPo(saved, objectMapper);
        resolveAgreement(plan.getAgreementId()).ifPresent(agreement -> {
            serviceJourneyTaskService.createDailyCareTask(agreement.getApplicationId(), agreement.getId(), plan.getElderId());
            if (CaregiverCheckInRecord.STATUS_COMPLETED.equals(savedDomain.getCompletionStatus())) {
                serviceJourneyTaskService.completeOpenTask(agreement.getApplicationId(), ServiceJourneyTaskService.TASK_TYPE_DAILY_CARE_TASK_EXECUTION);
            }
        });
        serviceJourneyTransitionLogService.logBusinessAction(
                BUSINESS_TYPE_CHECK_IN,
                saved.getId(),
                saved.getElderId(),
                ACTION_CHECK_IN,
                savedDomain.getCompletionStatus(),
                dto);
        return savedDomain.toDto();
    }

    @Transactional(readOnly = true)
    public List<CaregiverCheckInRecordDto> listMyCheckIns(Long elderId, LocalDate taskDate) {
        careDeliveryAuthorizationPolicy.requireMyCheckInListPermission();
        Long caregiverId = careDeliveryAuthorizationPolicy.requireCurrentUserId();

        Specification<CaregiverCheckInRecordPo> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("caregiverId"), caregiverId));
            if (elderId != null) {
                predicates.add(cb.equal(root.get("elderId"), elderId));
            }
            if (taskDate != null) {
                predicates.add(cb.equal(root.get("taskDate"), taskDate));
            }
            query.orderBy(cb.desc(root.get("taskDate")), cb.desc(root.get("id")));
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        return caregiverCheckInRecordRepository.findAll(specification).stream()
                .map(po -> CaregiverCheckInRecord.fromPo(po, objectMapper).toDto())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NurseCareRecordDto> listNurseCareRecords(Long elderId, Long nurseId, LocalDate recordDate) {
        careDeliveryAuthorizationPolicy.requireNurseCareRecordListPermission();
        return nurseCareRecordRepository.findAll(buildNurseRecordSpecification(elderId, nurseId, recordDate), Sort.by(Sort.Direction.DESC, "recordDate", "id"))
                .stream()
                .map(po -> NurseCareRecord.fromPo(po, objectMapper).toDto())
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public NurseCareRecordDto createNurseCareRecord(NurseCareRecordSaveDto dto) {
        careDeliveryAuthorizationPolicy.requireNurseCareRecordCreatePermission();
        Long nurseId = careDeliveryAuthorizationPolicy.requireCurrentUserId();
        String nurseName = careDeliveryAuthorizationPolicy.requireCurrentUsername();

        NurseCareRecord domain = NurseCareRecord.create(nurseId, nurseName, resolveElderName(dto.getElderId()), dto);
        NurseCareRecordPo saved = nurseCareRecordRepository.save(domain.toPo(objectMapper));
        NurseCareRecord savedDomain = NurseCareRecord.fromPo(saved, objectMapper);
        resolveAgreementByElderId(dto.getElderId()).ifPresent(agreement -> {
            serviceJourneyTaskService.createNurseCareRecordTask(agreement.getApplicationId(), agreement.getId(), dto.getElderId());
            serviceJourneyTaskService.completeOpenTask(agreement.getApplicationId(), ServiceJourneyTaskService.TASK_TYPE_NURSE_CARE_RECORD);
        });
        serviceJourneyTransitionLogService.logBusinessAction(
                BUSINESS_TYPE_NURSE_RECORD,
                saved.getId(),
                saved.getElderId(),
                ACTION_NURSE_RECORD_CREATED,
                null,
                dto);
        return savedDomain.toDto();
    }

    @Transactional(rollbackFor = Exception.class)
    public NurseCareRecordDto updateNurseCareRecord(Long recordId, NurseCareRecordSaveDto dto) {
        careDeliveryAuthorizationPolicy.requireNurseCareRecordUpdatePermission();

        NurseCareRecordPo po = nurseCareRecordRepository.findByIdForUpdate(recordId)
                .orElseThrow(() -> new IllegalArgumentException("未找到护理记录"));
        NurseCareRecord domain = NurseCareRecord.fromPo(po, objectMapper);
        domain.updateFrom(dto);
        domain.setElderName(resolveElderName(dto.getElderId()));
        domain.applyTo(po, objectMapper);
        NurseCareRecordPo saved = nurseCareRecordRepository.save(po);
        serviceJourneyTransitionLogService.logBusinessAction(
                BUSINESS_TYPE_NURSE_RECORD,
                saved.getId(),
                saved.getElderId(),
                ACTION_NURSE_RECORD_UPDATED,
                null,
                dto);
        return NurseCareRecord.fromPo(saved, objectMapper).toDto();
    }

    @Transactional(readOnly = true)
    public NurseCareRecordDto getNurseCareRecord(Long recordId) {
        careDeliveryAuthorizationPolicy.requireNurseCareRecordReadPermission();
        NurseCareRecordPo po = nurseCareRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("未找到护理记录"));
        return NurseCareRecord.fromPo(po, objectMapper).toDto();
    }

    @Transactional(readOnly = true)
    public List<FamilyServicePlanDto> listFamilyServicePlans(Long elderId) {
        careDeliveryAuthorizationPolicy.requireFamilyServicePlanListPermission();
        return carePlanRepository.findByElderIdAndStatusInOrderByPlanDateDescIdDesc(elderId, ACTIVE_PLAN_STATUSES).stream()
                .map(po -> {
                    CarePlan plan = CarePlan.fromPo(po, objectMapper);
                    Optional<ServiceAgreementPo> agreement = resolveAgreement(po.getAgreementId());
                    return FamilyServicePlanDto.builder()
                            .servicePlanId(plan.getPlanId())
                            .elderId(plan.getElderId())
                            .planName(plan.getPlanName())
                            .planItems(plan.getPlanItems())
                            .effectiveDate(agreement.map(ServiceAgreementPo::getEffectiveDate).orElse(plan.getPlanDate()))
                            .expireDate(agreement.map(ServiceAgreementPo::getExpiryDate).orElse(null))
                            .status("CLOSED".equals(plan.getStatus()) ? "INACTIVE" : "ACTIVE")
                            .assignedCaregiverId(plan.getAssignedCaregiverId())
                            .assignedCaregiverName(plan.getAssignedCaregiverName())
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CaregiverCheckInRecordDto> listFamilyCheckIns(Long elderId, LocalDate taskDate) {
        careDeliveryAuthorizationPolicy.requireFamilyCheckInListPermission();
        Specification<CaregiverCheckInRecordPo> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("elderId"), elderId));
            if (taskDate != null) {
                predicates.add(cb.equal(root.get("taskDate"), taskDate));
            }
            query.orderBy(cb.desc(root.get("taskDate")), cb.desc(root.get("id")));
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        return caregiverCheckInRecordRepository.findAll(specification).stream()
                .map(po -> CaregiverCheckInRecord.fromPo(po, objectMapper).toDto())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NurseCareRecordDto> listFamilyNurseCareRecords(Long elderId, LocalDate recordDate) {
        careDeliveryAuthorizationPolicy.requireFamilyNurseCareRecordListPermission();
        return nurseCareRecordRepository.findAll(buildNurseRecordSpecification(elderId, null, recordDate), Sort.by(Sort.Direction.DESC, "recordDate", "id"))
                .stream()
                .map(po -> NurseCareRecord.fromPo(po, objectMapper).toDto())
                .toList();
    }

    private DailyCareTaskDto buildDailyTask(CarePlanPo plan, Long caregiverId, LocalDate taskDate) {
        CarePlan domain = CarePlan.fromPo(plan, objectMapper);
        List<DailyCareTaskItemDto> taskItems = domain.getPlanItems();
        Optional<CaregiverCheckInRecordPo> latestCheckIn = caregiverCheckInRecordRepository
                .findTopByServicePlanIdAndCaregiverIdAndElderIdAndTaskDateOrderByIdDesc(plan.getId(), caregiverId, plan.getElderId(), taskDate);
        if (latestCheckIn.isPresent()) {
            taskItems = CaregiverCheckInRecord.fromPo(latestCheckIn.get(), objectMapper).getTaskItems();
        }
        return DailyCareTaskDto.builder()
                .elderId(plan.getElderId())
                .elderName(resolveElderName(plan.getElderId()))
                .servicePlanId(plan.getId())
                .taskDate(taskDate)
                .taskItems(taskItems)
                .build();
    }

    private Specification<NurseCareRecordPo> buildNurseRecordSpecification(Long elderId, Long nurseId, LocalDate recordDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (elderId != null) {
                predicates.add(cb.equal(root.get("elderId"), elderId));
            }
            if (nurseId != null) {
                predicates.add(cb.equal(root.get("nurseId"), nurseId));
            }
            if (recordDate != null) {
                predicates.add(cb.equal(root.get("recordDate"), recordDate));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void ensurePlanAssignedToCurrentCaregiver(CarePlanPo plan, Long caregiverId) {
        if (plan.getAssignedCaregiverId() == null) {
            throw new IllegalStateException("当前服务计划尚未分配护理员");
        }
        if (!caregiverId.equals(plan.getAssignedCaregiverId())) {
            throw new CareDeliveryAuthorizationException("当前用户无权处理该服务计划打卡");
        }
    }

    private String resolveElderName(Long elderId) {
        return healthCheckFormRepository.findTopByElderIdOrderByCheckDateDescIdDesc(elderId)
                .map(form -> form.getElderName() == null || form.getElderName().isBlank() ? "老人" + elderId : form.getElderName())
                .orElse("老人" + elderId);
    }

    private Optional<ServiceAgreementPo> resolveAgreement(Long agreementId) {
        if (agreementId == null) {
            return Optional.empty();
        }
        return serviceAgreementRepository.findById(agreementId);
    }

    private Optional<ServiceAgreementPo> resolveAgreementByElderId(Long elderId) {
        return serviceAgreementRepository.findTopByElderIdOrderByEffectiveDateDescIdDesc(elderId);
    }

    private void createPhaseThreeTasks(CarePlanPo plan) {
        resolveAgreement(plan.getAgreementId()).ifPresent(agreement -> {
            serviceJourneyTaskService.createDailyCareTask(agreement.getApplicationId(), agreement.getId(), plan.getElderId());
            serviceJourneyTaskService.createNurseCareRecordTask(agreement.getApplicationId(), agreement.getId(), plan.getElderId());
            serviceJourneyTaskService.createDoctorRoundRecordTask(agreement.getApplicationId(), agreement.getId(), plan.getElderId());
        });
    }
}
