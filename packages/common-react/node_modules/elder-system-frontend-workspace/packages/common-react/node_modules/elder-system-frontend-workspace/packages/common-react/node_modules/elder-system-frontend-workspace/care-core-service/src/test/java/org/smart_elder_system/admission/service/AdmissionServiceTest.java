package org.smart_elder_system.admission.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.smart_elder_system.admission.po.ServiceApplicationPo;
import org.smart_elder_system.admission.repository.ServiceApplicationRepository;
import org.smart_elder_system.common.dto.care.EligibilityAssessmentDTO;
import org.smart_elder_system.common.dto.care.ServiceApplicationDTO;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdmissionServiceTest {

    @Mock
    private ServiceApplicationRepository serviceApplicationRepository;

    @InjectMocks
    private AdmissionService admissionService;

    @Test
    void shouldSetActiveFlagWhenSubmittingApplication() {
        ServiceApplicationDTO request = new ServiceApplicationDTO();
        request.setElderId(1001L);
        request.setGuardianId(2001L);
        request.setApplicantName("张三");
        request.setContactPhone("13800000000");
        request.setServiceScene("HOME");
        request.setServiceRequest("助餐");

        when(serviceApplicationRepository.save(any())).thenAnswer(invocation -> {
            ServiceApplicationPo po = invocation.getArgument(0);
            po.setId(1L);
            return po;
        });

        ServiceApplicationDTO result = admissionService.submitApplication(request);

        assertEquals(1L, result.getApplicationId());
        ArgumentCaptor<ServiceApplicationPo> captor = ArgumentCaptor.forClass(ServiceApplicationPo.class);
        verify(serviceApplicationRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getActiveFlag());
    }

    @Test
    void shouldClearActiveFlagWhenAssessmentFails() {
        ServiceApplicationPo po = applicationPo(1L, org.smart_elder_system.admission.model.ServiceApplication.STATUS_SUBMITTED, 1);
        when(serviceApplicationRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(po));
        when(serviceApplicationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EligibilityAssessmentDTO assessment = new EligibilityAssessmentDTO();
        assessment.setApplicationId(1L);
        assessment.setEligible(false);
        assessment.setAssessmentConclusion("不符合条件");
        assessment.setAssessor("tester");

        ServiceApplicationDTO result = admissionService.assessEligibility(assessment);

        assertEquals(org.smart_elder_system.admission.model.ServiceApplication.STATUS_FAILED, result.getStatus());
        org.junit.jupiter.api.Assertions.assertNull(po.getActiveFlag());
    }

    @Test
    void shouldKeepActiveFlagWhenRevertingToAssessment() {
        ServiceApplicationPo po = applicationPo(1L, org.smart_elder_system.admission.model.ServiceApplication.STATUS_PASSED, 1);
        when(serviceApplicationRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(po));
        when(serviceApplicationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceApplicationDTO result = admissionService.revertToAssessment(1L, "补资料");

        assertEquals(org.smart_elder_system.admission.model.ServiceApplication.STATUS_ASSESSED, result.getStatus());
        assertEquals(1, po.getActiveFlag());
    }

    @Test
    void shouldClearActiveFlagWhenWithdrawingApplication() {
        ServiceApplicationPo po = applicationPo(1L, org.smart_elder_system.admission.model.ServiceApplication.STATUS_PASSED, 1);
        when(serviceApplicationRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(po));
        when(serviceApplicationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceApplicationDTO result = admissionService.withdrawApplication(1L, "主动撤回");

        assertEquals(org.smart_elder_system.admission.model.ServiceApplication.STATUS_WITHDRAWN, result.getStatus());
        org.junit.jupiter.api.Assertions.assertNull(po.getActiveFlag());
    }

    private ServiceApplicationPo applicationPo(Long id, String status, Integer activeFlag) {
        ServiceApplicationPo po = new ServiceApplicationPo();
        po.setId(id);
        po.setElderId(1001L);
        po.setGuardianId(2001L);
        po.setApplicantName("张三");
        po.setContactPhone("13800000000");
        po.setServiceScene("HOME");
        po.setServiceRequest("助餐");
        po.setStatus(status);
        po.setActiveFlag(activeFlag);
        return po;
    }
}
