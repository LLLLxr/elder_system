package org.smart_elder_system.health.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.smart_elder_system.common.dto.care.HealthAssessmentDTO;
import org.smart_elder_system.common.dto.care.HealthAssessmentRequestDTO;
import org.smart_elder_system.common.dto.care.HealthAssessmentSubmitDTO;
import org.smart_elder_system.common.dto.care.HealthCheckFormCreateRequestDTO;
import org.smart_elder_system.common.dto.care.HealthCheckFormDTO;
import org.smart_elder_system.common.dto.care.HealthProfileDTO;
import org.smart_elder_system.health.HealthAuthorizationException;
import org.smart_elder_system.health.service.HealthService;

import java.util.List;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Validated
public class HealthController {

    private final HealthService healthService;

    @GetMapping("/module-scope")
    public ResponseEntity<String> getModuleScope() {
        return ResponseEntity.ok(healthService.getModuleScope());
    }

    @PostMapping("/profiles")
    public ResponseEntity<HealthProfileDTO> createHealthProfile(@Valid @RequestBody HealthProfileDTO profileDTO) {
        return ResponseEntity.ok(healthService.createHealthProfile(profileDTO));
    }

    @PostMapping("/assessments")
    public ResponseEntity<HealthAssessmentDTO> performAssessment(@Valid @RequestBody HealthAssessmentDTO assessmentDTO) {
        return ResponseEntity.ok(healthService.performAssessment(assessmentDTO));
    }

    @PostMapping("/check-forms")
    public ResponseEntity<HealthCheckFormDTO> createHealthCheckForm() {
        throw new HealthAuthorizationException("健康体检表需由后台专业人员填写");
    }

    @PostMapping("/admin/check-forms")
    public ResponseEntity<HealthCheckFormDTO> createAdminHealthCheckForm(
            @Valid @RequestBody HealthCheckFormCreateRequestDTO healthCheckFormCreateRequestDTO) {
        return ResponseEntity.ok(healthService.createAdminHealthCheckForm(healthCheckFormCreateRequestDTO));
    }

    @GetMapping("/admin/check-forms/{formId}")
    public ResponseEntity<HealthCheckFormDTO> getAdminHealthCheckForm(@PathVariable @Min(1) Long formId) {
        return ResponseEntity.ok(healthService.getAdminHealthCheckForm(formId));
    }

    @GetMapping("/admin/check-forms/latest")
    public ResponseEntity<HealthCheckFormDTO> getLatestAdminHealthCheckForm(
            @RequestParam @Min(1) Long elderId,
            @RequestParam(required = false) @Min(1) Long agreementId,
            @RequestParam(required = false) @Min(1) Long authorUserId) {

        return ResponseEntity.ok(healthService.getLatestAdminHealthCheckForm(elderId, agreementId, authorUserId));
    }

    @GetMapping("/admin/check-forms")
    public ResponseEntity<List<HealthCheckFormDTO>> listAdminHealthCheckForms(
            @RequestParam(required = false) @Min(1) Long elderId,
            @RequestParam(required = false) @Min(1) Long agreementId,
            @RequestParam(required = false) @Min(1) Long authorUserId) {

        if (elderId == null && authorUserId == null) {
            throw new IllegalArgumentException("老人ID和填写人ID不能同时为空");
        }

        return ResponseEntity.ok(healthService.listAdminHealthCheckForms(elderId, agreementId, authorUserId));
    }

    @GetMapping("/check-forms/{formId}")
    public ResponseEntity<HealthCheckFormDTO> getHealthCheckForm(@PathVariable @Min(1) Long formId) {
        return ResponseEntity.ok(healthService.getHealthCheckForm(formId));
    }

    @GetMapping("/check-forms/latest")
    public ResponseEntity<HealthCheckFormDTO> getLatestHealthCheckForm(
            @RequestParam @Min(1) Long elderId,
            @RequestParam(required = false) @Min(1) Long agreementId) {

        return ResponseEntity.ok(healthService.getLatestHealthCheckForm(elderId, agreementId));
    }

    @GetMapping("/check-forms")
    public ResponseEntity<List<HealthCheckFormDTO>> listHealthCheckForms(
            @RequestParam(required = false) @Min(1) Long elderId,
            @RequestParam(required = false) @Min(1) Long agreementId) {

        if (elderId == null) {
            throw new IllegalArgumentException("老人ID不能为空");
        }

        return ResponseEntity.ok(healthService.listHealthCheckForms(elderId, agreementId));
    }

    @GetMapping("/assessment-requests/pending")
    public ResponseEntity<List<HealthAssessmentRequestDTO>> listPendingAssessmentRequests() {
        return ResponseEntity.ok(healthService.listPendingAssessmentRequests());
    }

    @GetMapping("/assessment-requests/history")
    public ResponseEntity<List<HealthAssessmentRequestDTO>> listAssessmentHistory() {
        return ResponseEntity.ok(healthService.listAssessmentHistory());
    }

    @PostMapping("/assessment-requests")
    public ResponseEntity<HealthAssessmentRequestDTO> submitPreSignAssessment(
            @Valid @RequestBody HealthAssessmentSubmitDTO submitDTO) {
        return ResponseEntity.ok(healthService.submitPreSignAssessment(submitDTO));
    }
}
