package org.smart_elder_system.careorchestration.feign;

import org.smart_elder_system.common.dto.care.EligibilityAssessmentDTO;
import org.smart_elder_system.common.dto.care.ServiceApplicationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "admission", url = "${services.admission.url}", path = "/admission")
public interface AdmissionClient {

    @PostMapping("/applications")
    ServiceApplicationDTO submitApplication(@RequestBody ServiceApplicationDTO applicationDTO);

    @PostMapping("/assessments")
    ServiceApplicationDTO assessEligibility(@RequestBody EligibilityAssessmentDTO assessmentDTO);

    @GetMapping("/applications/{applicationId}")
    ServiceApplicationDTO getApplication(@PathVariable("applicationId") Long applicationId);

    @PostMapping("/applications/{applicationId}/withdraw")
    ServiceApplicationDTO withdrawApplication(
            @PathVariable("applicationId") Long applicationId,
            @RequestParam(value = "reason", required = false) String reason);

    @PostMapping("/applications/{applicationId}/revert-to-assessment")
    ServiceApplicationDTO revertToAssessment(
            @PathVariable("applicationId") Long applicationId,
            @RequestParam(value = "reason", required = false) String reason);
}
