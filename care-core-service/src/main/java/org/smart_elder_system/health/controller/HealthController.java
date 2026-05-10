package org.smart_elder_system.health.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.smart_elder_system.common.dto.health.DoctorRoundRecordDto;
import org.smart_elder_system.common.dto.health.DoctorRoundRecordSaveDto;
import org.smart_elder_system.common.dto.health.HealthAssessmentDto;
import org.smart_elder_system.common.dto.health.HealthAssessmentRequestDto;
import org.smart_elder_system.common.dto.health.HealthAssessmentSubmitDto;
import org.smart_elder_system.common.dto.health.HealthCheckFormCreateRequestDto;
import org.smart_elder_system.common.dto.health.HealthCheckFormDto;
import org.smart_elder_system.common.dto.health.HealthProfileDto;
import org.smart_elder_system.health.HealthAuthorizationException;
import org.smart_elder_system.health.service.HealthService;

import java.time.LocalDate;
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
    public ResponseEntity<HealthProfileDto> createHealthProfile(@Valid @RequestBody HealthProfileDto profileDto) {
        return ResponseEntity.ok(healthService.createHealthProfile(profileDto));
    }

    @PostMapping("/assessments")
    public ResponseEntity<HealthAssessmentDto> performAssessment(@Valid @RequestBody HealthAssessmentDto assessmentDto) {
        return ResponseEntity.ok(healthService.performAssessment(assessmentDto));
    }

    @PostMapping("/check-forms")
    public ResponseEntity<HealthCheckFormDto> createHealthCheckForm() {
        throw new HealthAuthorizationException("健康体检表需由后台专业人员填写");
    }

    @PostMapping("/admin/check-forms")
    public ResponseEntity<HealthCheckFormDto> createAdminHealthCheckForm(
            @Valid @RequestBody HealthCheckFormCreateRequestDto healthCheckFormCreateRequestDto) {
        return ResponseEntity.ok(healthService.createAdminHealthCheckForm(healthCheckFormCreateRequestDto));
    }

    @GetMapping("/admin/check-forms/{formId}")
    public ResponseEntity<HealthCheckFormDto> getAdminHealthCheckForm(@PathVariable @Min(1) Long formId) {
        return ResponseEntity.ok(healthService.getAdminHealthCheckForm(formId));
    }

    @GetMapping("/admin/check-forms/latest")
    public ResponseEntity<HealthCheckFormDto> getLatestAdminHealthCheckForm(
            @RequestParam @Min(1) Long elderId,
            @RequestParam(required = false) @Min(1) Long agreementId,
            @RequestParam(required = false) @Min(1) Long authorUserId) {

        return ResponseEntity.ok(healthService.getLatestAdminHealthCheckForm(elderId, agreementId, authorUserId));
    }

    @GetMapping("/admin/check-forms")
    public ResponseEntity<List<HealthCheckFormDto>> listAdminHealthCheckForms(
            @RequestParam(required = false) @Min(1) Long elderId,
            @RequestParam(required = false) @Min(1) Long agreementId,
            @RequestParam(required = false) @Min(1) Long authorUserId) {

        if (elderId == null && authorUserId == null) {
            throw new IllegalArgumentException("老人ID和填写人ID不能同时为空");
        }

        return ResponseEntity.ok(healthService.listAdminHealthCheckForms(elderId, agreementId, authorUserId));
    }

    @GetMapping("/check-forms/{formId}")
    public ResponseEntity<HealthCheckFormDto> getHealthCheckForm(@PathVariable @Min(1) Long formId) {
        return ResponseEntity.ok(healthService.getHealthCheckForm(formId));
    }

    @GetMapping("/check-forms/latest")
    public ResponseEntity<HealthCheckFormDto> getLatestHealthCheckForm(
            @RequestParam @Min(1) Long elderId,
            @RequestParam(required = false) @Min(1) Long agreementId) {

        return ResponseEntity.ok(healthService.getLatestHealthCheckForm(elderId, agreementId));
    }

    @GetMapping("/check-forms")
    public ResponseEntity<List<HealthCheckFormDto>> listHealthCheckForms(
            @RequestParam(required = false) @Min(1) Long elderId,
            @RequestParam(required = false) @Min(1) Long agreementId) {

        if (elderId == null) {
            throw new IllegalArgumentException("老人ID不能为空");
        }

        return ResponseEntity.ok(healthService.listHealthCheckForms(elderId, agreementId));
    }

    @GetMapping("/doctor-round-records")
    public ResponseEntity<List<DoctorRoundRecordDto>> listDoctorRoundRecords(
            @RequestParam(required = false) @Min(1) Long elderId,
            @RequestParam(required = false) @Min(1) Long doctorId,
            @RequestParam(required = false) LocalDate roundDate) {
        return ResponseEntity.ok(healthService.listDoctorRoundRecords(elderId, doctorId, roundDate));
    }

    @PostMapping("/doctor-round-records")
    public ResponseEntity<DoctorRoundRecordDto> createDoctorRoundRecord(
            @Valid @RequestBody DoctorRoundRecordSaveDto dto) {
        return ResponseEntity.ok(healthService.createDoctorRoundRecord(dto));
    }

    @PutMapping("/doctor-round-records/{recordId}")
    public ResponseEntity<DoctorRoundRecordDto> updateDoctorRoundRecord(
            @PathVariable @Min(1) Long recordId,
            @Valid @RequestBody DoctorRoundRecordSaveDto dto) {
        return ResponseEntity.ok(healthService.updateDoctorRoundRecord(recordId, dto));
    }

    @GetMapping("/doctor-round-records/{recordId}")
    public ResponseEntity<DoctorRoundRecordDto> getDoctorRoundRecord(@PathVariable @Min(1) Long recordId) {
        return ResponseEntity.ok(healthService.getDoctorRoundRecord(recordId));
    }

    @GetMapping("/family/doctor-round-records/{elderId}")
    public ResponseEntity<List<DoctorRoundRecordDto>> listFamilyDoctorRoundRecords(
            @PathVariable @Min(1) Long elderId,
            @RequestParam(required = false) LocalDate roundDate) {
        return ResponseEntity.ok(healthService.listFamilyDoctorRoundRecords(elderId, roundDate));
    }

    @GetMapping("/assessment-requests/pending")
    public ResponseEntity<List<HealthAssessmentRequestDto>> listPendingAssessmentRequests() {
        return ResponseEntity.ok(healthService.listPendingAssessmentRequests());
    }

    @GetMapping("/assessment-requests/history")
    public ResponseEntity<List<HealthAssessmentRequestDto>> listAssessmentHistory() {
        return ResponseEntity.ok(healthService.listAssessmentHistory());
    }

    @PostMapping("/assessment-requests")
    public ResponseEntity<HealthAssessmentRequestDto> submitPreSignAssessment(
            @Valid @RequestBody HealthAssessmentSubmitDto submitDto) {
        return ResponseEntity.ok(healthService.submitPreSignAssessment(submitDto));
    }
}
