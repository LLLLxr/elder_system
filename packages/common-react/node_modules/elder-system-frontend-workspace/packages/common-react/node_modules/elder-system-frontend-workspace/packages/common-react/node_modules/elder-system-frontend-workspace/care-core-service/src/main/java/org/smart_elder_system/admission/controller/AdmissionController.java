package org.smart_elder_system.admission.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.smart_elder_system.admission.model.ServiceApplication;
import org.smart_elder_system.admission.service.AdmissionService;
import org.smart_elder_system.common.dto.care.EligibilityAssessmentDTO;
import org.smart_elder_system.common.dto.care.ServiceApplicationDTO;

import java.util.List;

@RestController
@RequestMapping("/admission")
@RequiredArgsConstructor
@Validated
public class AdmissionController {

    private final AdmissionService admissionService;

    @GetMapping("/module-scope")
    public ResponseEntity<String> getModuleScope() {
        return ResponseEntity.ok(admissionService.getModuleScope());
    }

    @PostMapping("/applications")
    public ResponseEntity<ServiceApplicationDTO> submitApplication(@Valid @RequestBody ServiceApplicationDTO applicationDTO) {
        return ResponseEntity.ok(admissionService.submitApplication(applicationDTO));
    }

    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<ServiceApplicationDTO> getApplication(@PathVariable Long applicationId) {
        return ResponseEntity.ok(admissionService.getApplication(applicationId));
    }

    @GetMapping("/applications")
    public ResponseEntity<List<ServiceApplicationDTO>> listApplications(
            @RequestParam(defaultValue = ServiceApplication.STATUS_SUBMITTED) String status) {
        return ResponseEntity.ok(admissionService.listApplicationsByStatus(status));
    }

    @PostMapping("/assessments")
    public ResponseEntity<ServiceApplicationDTO> assessEligibility(@Valid @RequestBody EligibilityAssessmentDTO assessmentDTO) {
        return ResponseEntity.ok(admissionService.assessEligibility(assessmentDTO));
    }

    @PostMapping("/applications/{applicationId}/withdraw")
    public ResponseEntity<ServiceApplicationDTO> withdrawApplication(
            @PathVariable Long applicationId,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(admissionService.withdrawApplication(applicationId, reason));
    }

    @PostMapping("/applications/{applicationId}/revert-to-assessment")
    public ResponseEntity<ServiceApplicationDTO> revertToAssessment(
            @PathVariable Long applicationId,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(admissionService.revertToAssessment(applicationId, reason));
    }
}
