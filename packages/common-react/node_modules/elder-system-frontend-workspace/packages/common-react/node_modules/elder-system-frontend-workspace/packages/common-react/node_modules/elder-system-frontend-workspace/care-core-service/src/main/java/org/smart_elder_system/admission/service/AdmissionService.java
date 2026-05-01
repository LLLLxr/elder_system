package org.smart_elder_system.admission.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.smart_elder_system.admission.model.EligibilityAssessment;
import org.smart_elder_system.admission.model.ServiceApplication;
import org.smart_elder_system.admission.po.ServiceApplicationPo;
import org.smart_elder_system.admission.repository.ServiceApplicationRepository;
import org.smart_elder_system.common.dto.care.EligibilityAssessmentDTO;
import org.smart_elder_system.common.dto.care.ServiceApplicationDTO;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdmissionService {

    private final ServiceApplicationRepository serviceApplicationRepository;

    public String getModuleScope() {
        return "准入模块：负责服务申请、接待登记、准入评估与准入决策";
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceApplicationDTO submitApplication(ServiceApplicationDTO applicationDTO) {
        ServiceApplication domain = ServiceApplication.fromDTO(applicationDTO);
        domain.submit();

        ServiceApplicationPo po = domain.toPo();
        po.setActiveFlag(resolveActiveFlag(domain.getStatus()));
        ServiceApplicationPo saved = serviceApplicationRepository.save(po);
        return ServiceApplication.fromPo(saved).toDTO();
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceApplicationDTO assessEligibility(EligibilityAssessmentDTO assessmentDTO) {
        ServiceApplicationPo po = serviceApplicationRepository.findByIdForUpdate(assessmentDTO.getApplicationId())
                .orElseThrow(() -> new IllegalArgumentException("未找到服务申请"));

        ServiceApplication domain = ServiceApplication.fromPo(po);

        EligibilityAssessment assessment = new EligibilityAssessment();
        assessment.setEligible(assessmentDTO.getEligible());
        assessment.setAssessmentConclusion(assessmentDTO.getAssessmentConclusion());
        assessment.setAssessor(assessmentDTO.getAssessor());
        assessment.setAssessedAt(assessmentDTO.getAssessedAt());

        domain.assess(assessment);
        domain.applyTo(po);
        po.setActiveFlag(resolveActiveFlag(domain.getStatus()));
        ServiceApplicationPo saved = serviceApplicationRepository.save(po);

        return ServiceApplication.fromPo(saved).toDTO();
    }

    public ServiceApplicationDTO getApplication(Long applicationId) {
        ServiceApplicationPo po = serviceApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("未找到服务申请"));
        return ServiceApplication.fromPo(po).toDTO();
    }

    public List<ServiceApplicationDTO> listApplicationsByStatus(String status) {
        return serviceApplicationRepository.findByStatusOrderBySubmittedAtAsc(status).stream()
                .map(ServiceApplication::fromPo)
                .map(ServiceApplication::toDTO)
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceApplicationDTO withdrawApplication(Long applicationId, String reason) {
        ServiceApplicationPo po = serviceApplicationRepository.findByIdForUpdate(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("未找到服务申请"));

        ServiceApplication domain = ServiceApplication.fromPo(po);
        domain.withdraw(reason);
        domain.applyTo(po);
        po.setActiveFlag(resolveActiveFlag(domain.getStatus()));
        ServiceApplicationPo saved = serviceApplicationRepository.save(po);
        return ServiceApplication.fromPo(saved).toDTO();
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceApplicationDTO revertToAssessment(Long applicationId, String reason) {
        ServiceApplicationPo po = serviceApplicationRepository.findByIdForUpdate(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("未找到服务申请"));

        ServiceApplication domain = ServiceApplication.fromPo(po);
        domain.revertToAssessment(reason);
        domain.applyTo(po);
        po.setActiveFlag(resolveActiveFlag(domain.getStatus()));
        ServiceApplicationPo saved = serviceApplicationRepository.save(po);
        return ServiceApplication.fromPo(saved).toDTO();
    }

    private Integer resolveActiveFlag(String status) {
        if (ServiceApplication.STATUS_FAILED.equals(status) || ServiceApplication.STATUS_WITHDRAWN.equals(status)) {
            return null;
        }
        return 1;
    }
}
