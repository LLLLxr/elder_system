package org.smart_elder_system.health.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.smart_elder_system.admission.model.ServiceApplication;
import org.smart_elder_system.admission.po.ServiceApplicationPo;
import org.smart_elder_system.admission.repository.ServiceApplicationRepository;
import org.smart_elder_system.common.dto.care.HealthAssessmentDTO;
import org.smart_elder_system.common.dto.care.HealthAssessmentRequestDTO;
import org.smart_elder_system.common.dto.care.HealthAssessmentSubmitDTO;
import org.smart_elder_system.common.dto.care.HealthCheckFormCreateRequestDTO;
import org.smart_elder_system.common.dto.care.HealthCheckFormDTO;
import org.smart_elder_system.common.dto.care.HealthProfileDTO;
import org.smart_elder_system.health.HealthAuthorizationPolicy;
import org.smart_elder_system.health.model.HealthAssessmentRecord;
import org.smart_elder_system.health.model.HealthCheckForm;
import org.smart_elder_system.health.model.HealthProfile;
import org.smart_elder_system.health.po.HealthAssessmentRecordPo;
import org.smart_elder_system.health.po.HealthCheckFormPo;
import org.smart_elder_system.health.po.HealthProfilePo;
import org.smart_elder_system.health.repository.HealthAssessmentRecordRepository;
import org.smart_elder_system.health.repository.HealthCheckFormRepository;
import org.smart_elder_system.health.repository.HealthProfileRepository;

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

    private final HealthProfileRepository healthProfileRepository;
    private final HealthAssessmentRecordRepository healthAssessmentRecordRepository;
    private final HealthCheckFormRepository healthCheckFormRepository;
    private final ServiceApplicationRepository serviceApplicationRepository;
    private final HealthAuthorizationPolicy healthAuthorizationPolicy;

    public String getModuleScope() {
        return "健康管理模块：负责健康档案、健康评估、监测记录与干预建议";
    }

    @Transactional(rollbackFor = Exception.class)
    public HealthProfileDTO createHealthProfile(HealthProfileDTO healthProfileDTO) {
        Optional<HealthProfilePo> existing = healthProfileRepository.findByElderIdAndAgreementIdForUpdate(
                healthProfileDTO.getElderId(),
                healthProfileDTO.getAgreementId());
        if (existing.isPresent()) {
            HealthProfilePo profilePo = existing.get();
            mergeProfilePo(profilePo, healthProfileDTO);
            return HealthProfile.fromPo(profilePo).toDTO();
        }

        HealthProfile profile = HealthProfile.fromDTO(healthProfileDTO);
        profile.initialize();

        try {
            HealthProfilePo saved = healthProfileRepository.save(profile.toPo());
            return HealthProfile.fromPo(saved).toDTO();
        } catch (DataIntegrityViolationException exception) {
            HealthProfilePo existingProfile = healthProfileRepository.findTopByElderIdAndAgreementIdOrderByProfileDateDescIdDesc(
                            healthProfileDTO.getElderId(),
                            healthProfileDTO.getAgreementId())
                    .orElseThrow(() -> exception);
            mergeProfilePo(existingProfile, healthProfileDTO);
            return HealthProfile.fromPo(existingProfile).toDTO();
        }
    }

    public HealthAssessmentDTO performAssessment(HealthAssessmentDTO assessmentDTO) {
        HealthAssessmentRecord record = HealthAssessmentRecord.fromDTO(assessmentDTO);
        record.assess();

        HealthAssessmentRecordPo saved = healthAssessmentRecordRepository.save(record.toPo());
        record.setAssessmentId(saved.getId());
        return record.toDTO();
    }

    @Transactional(rollbackFor = Exception.class)
    public HealthCheckFormDTO createAdminHealthCheckForm(HealthCheckFormCreateRequestDTO healthCheckFormCreateRequestDTO) {
        healthAuthorizationPolicy.requireCheckFormCreatePermission();

        HealthCheckForm form = HealthCheckForm.fromCreateRequest(healthCheckFormCreateRequestDTO);
        form.setAuthorUserId(healthAuthorizationPolicy.requireCurrentUserId());
        form.initialize();

        return saveHealthCheckForm(form);
    }

    public HealthCheckFormDTO getAdminHealthCheckForm(Long formId) {
        healthAuthorizationPolicy.requireCheckFormReadPermission();
        return getHealthCheckForm(formId);
    }

    public HealthCheckFormDTO getLatestAdminHealthCheckForm(Long elderId, Long agreementId, Long authorUserId) {
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

        return HealthCheckForm.fromPo(optional.orElseThrow(() -> new IllegalArgumentException("未找到健康体检表"))).toDTO();
    }

    public List<HealthCheckFormDTO> listAdminHealthCheckForms(Long elderId, Long agreementId, Long authorUserId) {
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
                .map(HealthCheckForm::toDTO)
                .toList();
    }

    public HealthCheckFormDTO getHealthCheckForm(Long formId) {
        HealthCheckFormPo po = healthCheckFormRepository.findById(formId)
                .orElseThrow(() -> new IllegalArgumentException("未找到健康体检表"));
        return HealthCheckForm.fromPo(po).toDTO();
    }

    public HealthCheckFormDTO getLatestHealthCheckForm(Long elderId, Long agreementId) {
        Optional<HealthCheckFormPo> optional = agreementId == null
                ? healthCheckFormRepository.findTopByElderIdOrderByCheckDateDescIdDesc(elderId)
                : healthCheckFormRepository.findTopByElderIdAndAgreementIdOrderByCheckDateDescIdDesc(elderId, agreementId);

        HealthCheckFormPo po = optional.orElseThrow(() -> new IllegalArgumentException("未找到健康体检表"));
        return HealthCheckForm.fromPo(po).toDTO();
    }

    public List<HealthCheckFormDTO> listHealthCheckForms(Long elderId, Long agreementId) {
        List<HealthCheckFormPo> forms = agreementId == null
                ? healthCheckFormRepository.findByElderIdOrderByCheckDateDescIdDesc(elderId)
                : healthCheckFormRepository.findByElderIdAndAgreementIdOrderByCheckDateDescIdDesc(elderId, agreementId);

        return forms.stream()
                .map(HealthCheckForm::fromPo)
                .map(HealthCheckForm::toDTO)
                .toList();
    }

    public List<HealthAssessmentRequestDTO> listPendingAssessmentRequests() {
        List<ServiceApplicationPo> passedApplications = serviceApplicationRepository
                .findByStatusInOrderBySubmittedAtDesc(List.of(ServiceApplication.STATUS_PASSED));

        List<HealthAssessmentRequestDTO> pending = new ArrayList<>();
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

        pending.sort(Comparator.comparing(HealthAssessmentRequestDTO::getSubmittedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return pending;
    }

    public List<HealthAssessmentRequestDTO> listAssessmentHistory() {
        List<ServiceApplicationPo> candidateApplications = serviceApplicationRepository
                .findByStatusInOrderBySubmittedAtDesc(List.of(ServiceApplication.STATUS_PASSED, ServiceApplication.STATUS_FAILED));

        List<HealthAssessmentRequestDTO> history = new ArrayList<>();
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

        history.sort(Comparator.comparing(HealthAssessmentRequestDTO::getHealthAssessedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return history;
    }

    @Transactional(rollbackFor = Exception.class)
    public HealthAssessmentRequestDTO submitPreSignAssessment(HealthAssessmentSubmitDTO submitDTO) {
        ServiceApplicationPo application = serviceApplicationRepository.findByIdForUpdate(submitDTO.getApplicationId())
                .orElseThrow(() -> new IllegalArgumentException("未找到服务申请"));

        if (!ServiceApplication.STATUS_PASSED.equals(application.getStatus())) {
            throw new IllegalArgumentException("当前申请状态不允许提交健康评估");
        }

        HealthCheckFormPo latestCheckForm = healthCheckFormRepository
                .findTopByElderIdOrderByCheckDateDescIdDesc(application.getElderId())
                .orElseThrow(() -> new IllegalArgumentException("请先提交健康体检表"));

        if (findLatestPreSignAssessment(submitDTO.getApplicationId(), application.getElderId(), application.getSubmittedAt()).isPresent()) {
            throw new IllegalArgumentException("该申请已完成健康评估");
        }

        HealthAssessmentRecord record = HealthAssessmentRecord.builder()
                .applicationId(submitDTO.getApplicationId())
                .elderId(application.getElderId())
                .agreementId(latestCheckForm.getAgreementId())
                .assessmentType(Boolean.TRUE.equals(submitDTO.getPassed()) ? ASSESSMENT_TYPE_PRE_SIGN_PASS : ASSESSMENT_TYPE_PRE_SIGN_FAIL)
                .conclusion(submitDTO.getAssessmentConclusion() + "（评估人：" + submitDTO.getAssessor() + "，责任医生：" + submitDTO.getResponsibleDoctor() + "）")
                .score(submitDTO.getScore())
                .build();
        record.assess();

        try {
            HealthAssessmentRecordPo saved = healthAssessmentRecordRepository.save(record.toPo());
            return toAssessmentRequest(application, latestCheckForm, saved);
        } catch (DataIntegrityViolationException exception) {
            HealthAssessmentRecordPo existing = findLatestPreSignAssessment(
                    submitDTO.getApplicationId(),
                    application.getElderId(),
                    application.getSubmittedAt()).orElseThrow(() -> exception);
            return toAssessmentRequest(application, latestCheckForm, existing);
        }
    }

    private HealthCheckFormDTO saveHealthCheckForm(HealthCheckForm form) {
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

        return form.toDTO();
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

    private HealthAssessmentRequestDTO toAssessmentRequest(
            ServiceApplicationPo application,
            HealthCheckFormPo checkForm,
            HealthAssessmentRecordPo assessmentRecord) {
        HealthAssessmentRequestDTO dto = new HealthAssessmentRequestDTO();
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

    private void mergeProfilePo(HealthProfilePo profilePo, HealthProfileDTO healthProfileDTO) {
        if (healthProfileDTO.getBloodType() != null) {
            profilePo.setBloodType(healthProfileDTO.getBloodType());
        }
        if (healthProfileDTO.getChronicDiseaseSummary() != null) {
            profilePo.setChronicDiseaseSummary(healthProfileDTO.getChronicDiseaseSummary());
        }
        if (healthProfileDTO.getAllergySummary() != null) {
            profilePo.setAllergySummary(healthProfileDTO.getAllergySummary());
        }
        if (healthProfileDTO.getRiskLevel() != null) {
            profilePo.setRiskLevel(healthProfileDTO.getRiskLevel());
        }
        if (healthProfileDTO.getProfileDate() != null) {
            profilePo.setProfileDate(healthProfileDTO.getProfileDate());
        }
        healthProfileRepository.save(profilePo);
    }
}

