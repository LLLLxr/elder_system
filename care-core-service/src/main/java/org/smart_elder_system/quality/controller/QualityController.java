package org.smart_elder_system.quality.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.smart_elder_system.common.dto.quality.CaregiverQualificationApplicationDto;
import org.smart_elder_system.common.dto.quality.CaregiverQualificationReviewDto;
import org.smart_elder_system.common.dto.quality.ServiceReviewDto;
import org.smart_elder_system.quality.service.QualityService;

import java.util.List;

@RestController
@RequestMapping("/quality")
@RequiredArgsConstructor
@Validated
public class QualityController {

    private final QualityService qualityService;

    @GetMapping("/module-scope")
    public ResponseEntity<String> getModuleScope() {
        return ResponseEntity.ok(qualityService.getModuleScope());
    }

    @PostMapping("/reviews")
    public ResponseEntity<ServiceReviewDto> reviewService(@Valid @RequestBody ServiceReviewDto reviewDto) {
        return ResponseEntity.ok(qualityService.reviewService(reviewDto));
    }

    @PostMapping("/caregiver-qualification-applications")
    public ResponseEntity<CaregiverQualificationApplicationDto> submitCaregiverQualificationApplication(
            @Valid @RequestBody CaregiverQualificationApplicationDto dto) {
        return ResponseEntity.ok(qualityService.submitCaregiverQualificationApplication(dto));
    }

    @GetMapping("/caregiver-qualification-applications/my")
    public ResponseEntity<List<CaregiverQualificationApplicationDto>> listMyCaregiverQualificationApplications() {
        return ResponseEntity.ok(qualityService.listMyCaregiverQualificationApplications());
    }

    @GetMapping("/caregiver-qualification-applications")
    public ResponseEntity<List<CaregiverQualificationApplicationDto>> listCaregiverQualificationApplications(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(qualityService.listCaregiverQualificationApplications(status));
    }

    @GetMapping("/caregiver-qualification-applications/{id}")
    public ResponseEntity<CaregiverQualificationApplicationDto> getCaregiverQualificationApplicationDetail(@PathVariable Long id) {
        return ResponseEntity.ok(qualityService.getCaregiverQualificationApplicationDetail(id));
    }

    @PostMapping("/caregiver-qualification-applications/{id}/approve")
    public ResponseEntity<CaregiverQualificationApplicationDto> approveCaregiverQualificationApplication(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) CaregiverQualificationReviewDto reviewDto) {
        return ResponseEntity.ok(qualityService.approveCaregiverQualificationApplication(id, reviewDto));
    }

    @PostMapping("/caregiver-qualification-applications/{id}/reject")
    public ResponseEntity<CaregiverQualificationApplicationDto> rejectCaregiverQualificationApplication(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) CaregiverQualificationReviewDto reviewDto) {
        return ResponseEntity.ok(qualityService.rejectCaregiverQualificationApplication(id, reviewDto));
    }
}
