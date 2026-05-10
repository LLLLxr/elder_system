package org.smart_elder_system.careorchestration.feign;

import org.smart_elder_system.common.dto.admission.EligibilityAssessmentDto;
import org.smart_elder_system.common.dto.admission.ServiceApplicationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "admission", url = "${services.admission.url}", path = "/admission")
public interface AdmissionClient {

    @PostMapping("/applications")
    ServiceApplicationDto submitApplication(@RequestBody ServiceApplicationDto applicationDto);

    @PostMapping("/assessments")
    ServiceApplicationDto assessEligibility(@RequestBody EligibilityAssessmentDto assessmentDto);

    @GetMapping("/applications/{applicationId}")
    ServiceApplicationDto getApplication(@PathVariable("applicationId") Long applicationId);

    @PostMapping("/applications/{applicationId}/withdraw")
    ServiceApplicationDto withdrawApplication(
            @PathVariable("applicationId") Long applicationId,
            @RequestParam(value = "reason", required = false) String reason);

    @PostMapping("/applications/{applicationId}/revert-to-assessment")
    ServiceApplicationDto revertToAssessment(
            @PathVariable("applicationId") Long applicationId,
            @RequestParam(value = "reason", required = false) String reason);
}
