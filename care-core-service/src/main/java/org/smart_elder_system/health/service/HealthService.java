package org.smart_elder_system.health.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.smart_elder_system.admission.vo.ServiceApplication;
import org.smart_elder_system.admission.po.ServiceApplicationPo;
import org.smart_elder_system.admission.repository.ServiceApplicationRepository;
import org.smart_elder_system.careorchestration.service.ServiceJourneyTaskService;
import org.smart_elder_system.careorchestration.service.ServiceJourneyTransitionLogService;
import org.smart_elder_system.common.dto.health.DoctorRoundRecordDto;
import org.smart_elder_system.common.dto.health.DoctorRoundRecordSaveDto;
import org.smart_elder_system.common.dto.health.HealthAssessmentDto;
import org.smart_elder_system.common.dto.health.HealthAssessmentRequestDto;
import org.smart_elder_system.common.dto.health.HealthAssessmentSubmitDto;
import org.smart_elder_system.common.dto.health.HealthCheckFormCreateRequestDto;
import org.smart_elder_system.common.dto.health.HealthCheckFormDto;
import org.smart_elder_system.common.dto.health.HealthProfileDto;
import org.smart_elder_system.contract.po.ServiceAgreementPo;
import org.smart_elder_system.contract.repository.ServiceAgreementRepository;
import org.smart_elder_system.health.HealthAuthorizationPolicy;
import org.smart_elder_system.health.vo.DoctorRoundRecord;
import org.smart_elder_system.health.vo.HealthAssessmentRecord;
import org.smart_elder_system.health.vo.HealthCheckForm;
import org.smart_elder_system.health.vo.HealthProfile;
import org.smart_elder_system.health.po.DoctorRoundRecordPo;
import org.smart_elder_system.health.po.HealthAssessmentRecordPo;
import org.smart_elder_system.health.po.HealthCheckFormPo;
import org.smart_elder_system.health.po.HealthProfilePo;
import org.smart_elder_system.health.repository.DoctorRoundRecordRepository;
import org.smart_elder_system.health.repository.HealthAssessmentRecordRepository;
import org.smart_elder_system.health.repository.HealthCheckFormRepository;
import org.smart_elder_system.health.repository.HealthProfileRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HealthService {

    private static final String ASSESSMENT_TYPE_PRE_SIGN_PASS = "PRE_SIGN_PASS";
    private static final String ASSESSMENT_TYPE_PRE_SIGN_FAIL = "PRE_SIGN_FAIL";
    private static final String BUSINESS_TYPE_DOCTOR_ROUND_RECORD = "DOCTOR_ROUND_RECORD";
    private static final String ACTION_DOCTOR_ROUND_RECORD_CREATED = "DOCTOR_ROUND_RECORD_CREATED";
    private static final String ACTION_DOCTOR_ROUND_RECORD_UPDATED = "DOCTOR_ROUND_RECORD_UPDATED";

    private final HealthProfileRepository healthProfileRepository;
    private final HealthAssessmentRecordRepository healthAssessmentRecordRepository;
    private final HealthCheckFormRepository healthCheckFormRepository;
    private final DoctorRoundRecordRepository doctorRoundRecordRepository;
    private final ServiceApplicationRepository serviceApplicationRepository;
    private final ServiceAgreementRepository serviceAgreementRepository;
    private final HealthAuthorizationPolicy healthAuthorizationPolicy;
    private final ServiceJourneyTaskService serviceJourneyTaskService;
    private final ServiceJourneyTransitionLogService serviceJourneyTransitionLogService;

    public String getModuleScope() {
        return "健康管理模块：负责健康档案、健康评估、监测记录与干预建议";
    }

    @Transactional(rollbackFor = Exception.class)
    public HealthProfileDto createHealthProfile(HealthProfileDto healthProfileDto) {
        Optional<HealthProfilePo> existing = healthProfileRepository.findByElderIdAndAgreementIdForUpdate(
                healthProfileDto.getElderId(),
                healthProfileDto.getAgreementId());
        if (existing.isPresent()) {
            HealthProfilePo profilePo = existing.get();
            mergeProfilePo(profilePo, healthProfileDto);
            return HealthProfile.fromPo(profilePo).toDto();
        }

        HealthProfile profile = HealthProfile.fromDto(healthProfileDto);
        profile.initialize();

        try {
            HealthProfilePo saved = healthProfileRepository.save(profile.toPo());
            return HealthProfile.fromPo(saved).toDto();
        } catch (DataIntegrityViolationException exception) {
            HealthProfilePo existingProfile = healthProfileRepository.findTopByElderIdAndAgreementIdOrderByProfileDateDescIdDesc(
                            healthProfileDto.getElderId(),
                            healthProfileDto.getAgreementId())
                    .orElseThrow(() -> exception);
            mergeProfilePo(existingProfile, healthProfileDto);
            return HealthProfile.fromPo(existingProfile).toDto();
        }
    }

    public HealthAssessmentDto performAssessment(HealthAssessmentDto assessmentDto) {
        HealthAssessmentRecord record = HealthAssessmentRecord.fromDto(assessmentDto);
        record.assess();

        HealthAssessmentRecordPo saved = healthAssessmentRecordRepository.save(record.toPo());
        record.setAssessmentId(saved.getId());
        return record.toDto();
    }

    @Transactional(rollbackFor = Exception.class)
    public HealthCheckFormDto createAdminHealthCheckForm(HealthCheckFormCreateRequestDto healthCheckFormCreateRequestDto) {
        healthAuthorizationPolicy.requireCheckFormCreatePermission();

        HealthCheckForm form = HealthCheckForm.fromCreateRequest(healthCheckFormCreateRequestDto);
        form.setAuthorUserId(healthAuthorizationPolicy.requireCurrentUserId());
        form.initialize();

        return saveHealthCheckForm(form);
    }

    public HealthCheckFormDto getAdminHealthCheckForm(Long formId) {
        healthAuthorizationPolicy.requireCheckFormReadPermission();
        return getHealthCheckForm(formId);
    }

    public HealthCheckFormDto getLatestAdminHealthCheckForm(Long elderId, Long agreementId, Long authorUserId) {
        healthAuthorizationPolicy.requireCheckFormReadPermission();

        Optional<HealthCheckFormPo> optional = Optional.empty();
        if (authorUserId != null) {
            optional = healthCheckFormRepository.findTopByElderIdAndAuthorUserIdOrderByCheckDateDescIdDesc(elderId, authorUserId);
        }
        if (optional.isEmpty() && agreementId != null) {
            optional = healthCheckFormRepository.findTopByElderIdAndAgreementIdOrderByCheckDateDescIdDesc(elderId, agreementId);
        }
        if (optional.isEmpty()) {
            optional = healthCheckFormRepository.findTopByElderIdOrderByCheckDateDescIdDesc(elderId);
        }

        return HealthCheckForm.fromPo(optional.orElseThrow(() -> new IllegalArgumentException("未找到健康体检表"))).toDto();
    }

    public List<HealthCheckFormDto> listAdminHealthCheckForms(Long elderId, Long agreementId, Long authorUserId) {
        healthAuthorizationPolicy.requireCheckFormListPermission();

        List<HealthCheckFormPo> forms;
        if (authorUserId != null && elderId != null) {
            forms = healthCheckFormRepository.findByElderIdAndAuthorUserIdOrderByCheckDateDescIdDesc(elderId, authorUserId);
        } else if (authorUserId != null) {
            forms = healthCheckFormRepository.findByAuthorUserIdOrderByCheckDateDescIdDesc(authorUserId);
        } else if (agreementId != null) {
            forms = healthCheckFormRepository.findByElderIdAndAgreementIdOrderByCheckDateDescIdDesc(elderId, agreementId);
        } else {
            forms = healthCheckFormRepository.findByElderIdOrderByCheckDateDescIdDesc(elderId);
        }

        return forms.stream()
                .map(HealthCheckForm::fromPo)
                .map(HealthCheckForm::toDto)
                .toList();
    }

    public HealthCheckFormDto getHealthCheckForm(Long formId) {
        HealthCheckFormPo po = healthCheckFormRepository.findById(formId)
                .orElseThrow(() -> new IllegalArgumentException("未找到健康体检表"));
        return HealthCheckForm.fromPo(po).toDto();
    }

    public HealthCheckFormDto getLatestHealthCheckForm(Long elderId, Long agreementId) {
        Optional<HealthCheckFormPo> optional = agreementId == null
                ? healthCheckFormRepository.findTopByElderIdOrderByCheckDateDescIdDesc(elderId)
                : healthCheckFormRepository.findTopByElderIdAndAgreementIdOrderByCheckDateDescIdDesc(elderId, agreementId);

        HealthCheckFormPo po = optional.orElseThrow(() -> new IllegalArgumentException("未找到健康体检表"));
        return HealthCheckForm.fromPo(po).toDto();
    }

    public List<HealthCheckFormDto> listHealthCheckForms(Long elderId, Long agreementId) {
        List<HealthCheckFormPo> forms = agreementId == null
                ? healthCheckFormRepository.findByElderIdOrderByCheckDateDescIdDesc(elderId)
                : healthCheckFormRepository.findByElderIdAndAgreementIdOrderByCheckDateDescIdDesc(elderId, agreementId);

        return forms.stream()
                .map(HealthCheckForm::fromPo)
                .map(HealthCheckForm::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DoctorRoundRecordDto> listDoctorRoundRecords(Long elderId, Long doctorId, LocalDate roundDate) {
        healthAuthorizationPolicy.requireDoctorRoundRecordListPermission();
        return doctorRoundRecordRepository.findAll(
                        buildDoctorRoundRecordSpecification(elderId, doctorId, roundDate),
                        Sort.by(Sort.Direction.DESC, "roundTime", "id"))
                .stream()
                .map(DoctorRoundRecord::fromPo)
                .map(DoctorRoundRecord::toDto)
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public DoctorRoundRecordDto createDoctorRoundRecord(DoctorRoundRecordSaveDto dto) {
        healthAuthorizationPolicy.requireDoctorRoundRecordCreatePermission();
        Long doctorId = healthAuthorizationPolicy.requireCurrentUserId();
        String doctorName = healthAuthorizationPolicy.requireCurrentUsername();

        DoctorRoundRecord domain = DoctorRoundRecord.create(doctorId, doctorName, resolveElderName(dto.getElderId()), dto);
        DoctorRoundRecordPo saved = doctorRoundRecordRepository.save(domain.toPo());
        DoctorRoundRecord savedDomain = DoctorRoundRecord.fromPo(saved);
        resolveAgreementByElderId(dto.getElderId()).ifPresent(agreement -> {
            serviceJourneyTaskService.createDoctorRoundRecordTask(agreement.getApplicationId(), agreement.getId(), dto.getElderId());
            serviceJourneyTaskService.completeOpenTask(agreement.getApplicationId(), ServiceJourneyTaskService.TASK_TYPE_DOCTOR_ROUND_RECORD);
        });
        serviceJourneyTransitionLogService.logBusinessAction(
                BUSINESS_TYPE_DOCTOR_ROUND_RECORD,
                saved.getId(),
                saved.getElderId(),
                ACTION_DOCTOR_ROUND_RECORD_CREATED,
                null,
                dto);
        return savedDomain.toDto();
    }

    @Transactional(rollbackFor = Exception.class)
    public DoctorRoundRecordDto updateDoctorRoundRecord(Long recordId, DoctorRoundRecordSaveDto dto) {
        healthAuthorizationPolicy.requireDoctorRoundRecordUpdatePermission();

        DoctorRoundRecordPo po = doctorRoundRecordRepository.findByIdForUpdate(recordId)
                .orElseThrow(() -> new IllegalArgumentException("未找到查房记录"));
        DoctorRoundRecord domain = DoctorRoundRecord.fromPo(po);
        domain.updateFrom(dto);
        domain.setElderName(resolveElderName(dto.getElderId()));
        domain.applyTo(po);
        DoctorRoundRecordPo saved = doctorRoundRecordRepository.save(po);
        serviceJourneyTransitionLogService.logBusinessAction(
                BUSINESS_TYPE_DOCTOR_ROUND_RECORD,
                saved.getId(),
                saved.getElderId(),
                ACTION_DOCTOR_ROUND_RECORD_UPDATED,
                null,
                dto);
        return DoctorRoundRecord.fromPo(saved).toDto();
    }

    @Transactional(readOnly = true)
    public DoctorRoundRecordDto getDoctorRoundRecord(Long recordId) {
        healthAuthorizationPolicy.requireDoctorRoundRecordReadPermission();
        DoctorRoundRecordPo po = doctorRoundRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("未找到查房记录"));
        return DoctorRoundRecord.fromPo(po).toDto();
    }

    @Transactional(readOnly = true)
    public List<DoctorRoundRecordDto> listFamilyDoctorRoundRecords(Long elderId, LocalDate roundDate) {
        healthAuthorizationPolicy.requireFamilyDoctorRoundRecordListPermission();
        return doctorRoundRecordRepository.findAll(
                        buildDoctorRoundRecordSpecification(elderId, null, roundDate),
                        Sort.by(Sort.Direction.DESC, "roundTime", "id"))
                .stream()
                .map(DoctorRoundRecord::fromPo)
                .map(DoctorRoundRecord::toDto)
                .toList();
    }

    public List<HealthAssessmentRequestDto> listPendingAssessmentRequests() {
        List<ServiceApplicationPo> passedApplications = serviceApplicationRepository
                .findByStatusInOrderBySubmittedAtDesc(List.of(ServiceApplication.STATUS_PASSED));

        List<HealthAssessmentRequestDto> pending = new ArrayList<>();
        for (ServiceApplicationPo application : passedApplications) {
            Optional<HealthCheckFormPo> latestCheckForm = healthCheckFormRepository
                    .findTopByElderIdOrderByCheckDateDescIdDesc(application.getElderId());
            if (latestCheckForm.isEmpty()) {
                continue;
            }

            if (findLatestPreSignAssessment(application.getId(), application.getElderId(), application.getSubmittedAt()).isPresent()) {
                continue;
            }

            pending.add(toAssessmentRequest(application, latestCheckForm.get(), null));
        }

        pending.sort(Comparator.comparing(HealthAssessmentRequestDto::getSubmittedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return pending;
    }

    public List<HealthAssessmentRequestDto> listAssessmentHistory() {
        List<ServiceApplicationPo> candidateApplications = serviceApplicationRepository
                .findByStatusInOrderBySubmittedAtDesc(List.of(ServiceApplication.STATUS_PASSED, ServiceApplication.STATUS_FAILED));

        List<HealthAssessmentRequestDto> history = new ArrayList<>();
        for (ServiceApplicationPo application : candidateApplications) {
            Optional<HealthCheckFormPo> latestCheckForm = healthCheckFormRepository
                    .findTopByElderIdOrderByCheckDateDescIdDesc(application.getElderId());
            if (latestCheckForm.isEmpty()) {
                continue;
            }

            Optional<HealthAssessmentRecordPo> record = findLatestPreSignAssessment(
                    application.getId(),
                    application.getElderId(),
                    application.getSubmittedAt());
            if (record.isEmpty()) {
                continue;
            }

            history.add(toAssessmentRequest(application, latestCheckForm.get(), record.get()));
        }

        history.sort(Comparator.comparing(HealthAssessmentRequestDto::getHealthAssessedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return history;
    }

    @Transactional(rollbackFor = Exception.class)
    public HealthAssessmentRequestDto submitPreSignAssessment(HealthAssessmentSubmitDto submitDto) {
        ServiceApplicationPo application = serviceApplicationRepository.findByIdForUpdate(submitDto.getApplicationId())
                .orElseThrow(() -> new IllegalArgumentException("未找到服务申请"));

        if (!ServiceApplication.STATUS_PASSED.equals(application.getStatus())) {
            throw new IllegalArgumentException("当前申请状态不允许提交健康评估");
        }

        HealthCheckFormPo latestCheckForm = healthCheckFormRepository
                .findTopByElderIdOrderByCheckDateDescIdDesc(application.getElderId())
                .orElseThrow(() -> new IllegalArgumentException("请先提交健康体检表"));

        if (findLatestPreSignAssessment(submitDto.getApplicationId(), application.getElderId(), application.getSubmittedAt()).isPresent()) {
            throw new IllegalArgumentException("该申请已完成健康评估");
        }

        HealthAssessmentRecord record = HealthAssessmentRecord.builder()
                .applicationId(submitDto.getApplicationId())
                .elderId(application.getElderId())
                .agreementId(latestCheckForm.getAgreementId())
                .assessmentType(Boolean.TRUE.equals(submitDto.getPassed()) ? ASSESSMENT_TYPE_PRE_SIGN_PASS : ASSESSMENT_TYPE_PRE_SIGN_FAIL)
                .conclusion(submitDto.getAssessmentConclusion() + "（评估人：" + submitDto.getAssessor() + "，责任医生：" + submitDto.getResponsibleDoctor() + "）")
                .score(submitDto.getScore())
                .build();
        record.assess();

        try {
            HealthAssessmentRecordPo saved = healthAssessmentRecordRepository.save(record.toPo());
            return toAssessmentRequest(application, latestCheckForm, saved);
        } catch (DataIntegrityViolationException exception) {
            HealthAssessmentRecordPo existing = findLatestPreSignAssessment(
                    submitDto.getApplicationId(),
                    application.getElderId(),
                    application.getSubmittedAt()).orElseThrow(() -> exception);
            return toAssessmentRequest(application, latestCheckForm, existing);
        }
    }

    private HealthCheckFormDto saveHealthCheckForm(HealthCheckForm form) {
        HealthCheckFormPo saved = healthCheckFormRepository.save(form.toPo());
        form.setFormId(saved.getId());

        Optional<HealthProfilePo> profileOptional = healthProfileRepository
                .findByElderIdAndAgreementIdForUpdate(form.getElderId(), form.getAgreementId());

        HealthProfilePo profilePo = profileOptional.orElseGet(HealthProfilePo::new);
        profilePo.setElderId(form.getElderId());
        profilePo.setAgreementId(form.getAgreementId());
        profilePo.setChronicDiseaseSummary(form.getChronicDiseaseSummary());
        profilePo.setAllergySummary(form.getAllergySummary());
        profilePo.setProfileDate(form.getCheckDate());
        try {
            healthProfileRepository.save(profilePo);
        } catch (DataIntegrityViolationException exception) {
            HealthProfilePo existingProfile = healthProfileRepository.findTopByElderIdAndAgreementIdOrderByProfileDateDescIdDesc(
                            form.getElderId(),
                            form.getAgreementId())
                    .orElseThrow(() -> exception);
            existingProfile.setChronicDiseaseSummary(form.getChronicDiseaseSummary());
            existingProfile.setAllergySummary(form.getAllergySummary());
            existingProfile.setProfileDate(form.getCheckDate());
            healthProfileRepository.save(existingProfile);
        }

        return form.toDto();
    }

    private Optional<HealthAssessmentRecordPo> findLatestPreSignAssessment(Long applicationId, Long elderId, LocalDateTime submittedAt) {
        if (applicationId != null) {
            Optional<HealthAssessmentRecordPo> byApplication = healthAssessmentRecordRepository
                    .findTopByApplicationIdAndAssessmentTypeInOrderByAssessedAtDescIdDesc(
                            applicationId,
                            List.of(ASSESSMENT_TYPE_PRE_SIGN_PASS, ASSESSMENT_TYPE_PRE_SIGN_FAIL));
            if (byApplication.isPresent()) {
                return byApplication;
            }
        }

        LocalDateTime lowerBound = submittedAt == null ? LocalDateTime.MIN : submittedAt;
        return healthAssessmentRecordRepository
                .findTopByElderIdAndAssessmentTypeInAndAssessedAtGreaterThanEqualOrderByAssessedAtDescIdDesc(
                        elderId,
                        List.of(ASSESSMENT_TYPE_PRE_SIGN_PASS, ASSESSMENT_TYPE_PRE_SIGN_FAIL),
                        lowerBound);
    }

    private HealthAssessmentRequestDto toAssessmentRequest(
            ServiceApplicationPo application,
            HealthCheckFormPo checkForm,
            HealthAssessmentRecordPo assessmentRecord) {
        HealthAssessmentRequestDto dto = new HealthAssessmentRequestDto();
        dto.setApplicationId(application.getId());
        dto.setElderId(application.getElderId());
        dto.setAgreementId(checkForm.getAgreementId());
        dto.setApplicantName(application.getApplicantName());
        dto.setServiceScene(application.getServiceScene());
        dto.setSubmittedAt(application.getSubmittedAt());
        dto.setNeedsAssessedAt(application.getAssessedAt());

        if (assessmentRecord == null) {
            dto.setAssessmentStatus("PENDING");
            return dto;
        }

        dto.setAssessmentStatus(ASSESSMENT_TYPE_PRE_SIGN_PASS.equals(assessmentRecord.getAssessmentType()) ? "PASSED" : "FAILED");
        dto.setAssessmentConclusion(assessmentRecord.getConclusion());
        dto.setScore(assessmentRecord.getScore());
        dto.setHealthAssessedAt(assessmentRecord.getAssessedAt());
        return dto;
    }

    private Specification<DoctorRoundRecordPo> buildDoctorRoundRecordSpecification(Long elderId, Long doctorId, LocalDate roundDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (elderId != null) {
                predicates.add(cb.equal(root.get("elderId"), elderId));
            }
            if (doctorId != null) {
                predicates.add(cb.equal(root.get("doctorId"), doctorId));
            }
            if (roundDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("roundTime"), roundDate.atStartOfDay()));
                predicates.add(cb.lessThan(root.get("roundTime"), roundDate.plusDays(1).atStartOfDay()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private String resolveElderName(Long elderId) {
        return healthCheckFormRepository.findTopByElderIdOrderByCheckDateDescIdDesc(elderId)
                .map(form -> form.getElderName() == null || form.getElderName().isBlank() ? "老人" + elderId : form.getElderName())
                .orElse("老人" + elderId);
    }

    private Optional<ServiceAgreementPo> resolveAgreementByElderId(Long elderId) {
        return serviceAgreementRepository.findTopByElderIdOrderByEffectiveDateDescIdDesc(elderId);
    }

    private void mergeProfilePo(HealthProfilePo profilePo, HealthProfileDto healthProfileDto) {
        if (healthProfileDto.getBloodType() != null) {
            profilePo.setBloodType(healthProfileDto.getBloodType());
        }
        if (healthProfileDto.getChronicDiseaseSummary() != null) {
            profilePo.setChronicDiseaseSummary(healthProfileDto.getChronicDiseaseSummary());
        }
        if (healthProfileDto.getAllergySummary() != null) {
            profilePo.setAllergySummary(healthProfileDto.getAllergySummary());
        }
        if (healthProfileDto.getRiskLevel() != null) {
            profilePo.setRiskLevel(healthProfileDto.getRiskLevel());
        }
        if (healthProfileDto.getProfileDate() != null) {
            profilePo.setProfileDate(healthProfileDto.getProfileDate());
        }
        healthProfileRepository.save(profilePo);
    }
}
