package org.smart_elder_system.careorchestration.feign;

import org.smart_elder_system.common.dto.care.HealthAssessmentDTO;
import org.smart_elder_system.common.dto.care.HealthAssessmentRequestDTO;
import org.smart_elder_system.common.dto.care.HealthAssessmentSubmitDTO;
import org.smart_elder_system.common.dto.care.HealthProfileDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "health", url = "${services.health.url}", path = "/health")
public interface HealthClient {

    @PostMapping("/profiles")
    HealthProfileDTO createHealthProfile(@RequestBody HealthProfileDTO healthProfileDTO);

    @PostMapping("/assessments")
    HealthAssessmentDTO performAssessment(@RequestBody HealthAssessmentDTO assessmentDTO);

    @GetMapping("/assessment-requests/history")
    List<HealthAssessmentRequestDTO> listAssessmentHistory();

    @PostMapping("/assessment-requests")
    HealthAssessmentRequestDTO submitPreSignAssessment(@RequestBody HealthAssessmentSubmitDTO submitDTO);
}
