package org.smart_elder_system.careorchestration.feign;

import org.smart_elder_system.common.dto.health.HealthAssessmentDto;
import org.smart_elder_system.common.dto.health.HealthAssessmentRequestDto;
import org.smart_elder_system.common.dto.health.HealthAssessmentSubmitDto;
import org.smart_elder_system.common.dto.health.HealthProfileDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "health", url = "${services.health.url}", path = "/health")
public interface HealthClient {

    @PostMapping("/profiles")
    HealthProfileDto createHealthProfile(@RequestBody HealthProfileDto healthProfileDto);

    @PostMapping("/assessments")
    HealthAssessmentDto performAssessment(@RequestBody HealthAssessmentDto assessmentDto);

    @GetMapping("/assessment-requests/history")
    List<HealthAssessmentRequestDto> listAssessmentHistory();

    @PostMapping("/assessment-requests")
    HealthAssessmentRequestDto submitPreSignAssessment(@RequestBody HealthAssessmentSubmitDto submitDto);
}
