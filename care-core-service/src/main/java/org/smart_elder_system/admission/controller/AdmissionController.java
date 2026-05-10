package org.smart_elder_system.admission.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.smart_elder_system.admission.vo.ServiceApplication;
import org.smart_elder_system.admission.service.AdmissionService;
import org.smart_elder_system.common.dto.admission.EligibilityAssessmentDto;
import org.smart_elder_system.common.dto.admission.FamilyVisitReservationDto;
import org.smart_elder_system.common.dto.admission.FamilyVisitReservationReviewDto;
import org.smart_elder_system.common.dto.admission.FamilyVisitReservationRuleDto;
import org.smart_elder_system.common.dto.admission.FamilyVisitSlotDto;
import org.smart_elder_system.common.dto.admission.ServiceApplicationDto;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admission")
@RequiredArgsConstructor
@Validated
@Tag(name = "入院管理", description = "提供服务申请、资格评估、家属预约参观等入院相关接口")
public class AdmissionController {

    private final AdmissionService admissionService;

    @GetMapping("/module-scope")
    public ResponseEntity<String> getModuleScope() {
        return ResponseEntity.ok(admissionService.getModuleScope());
    }

    @GetMapping("/family-visit-reservation-rules")
    public ResponseEntity<FamilyVisitReservationRuleDto> getFamilyVisitReservationRules() {
        return ResponseEntity.ok(admissionService.getFamilyVisitReservationRules());
    }

    @GetMapping("/family-visit-slots")
    @Operation(summary = "查询可预约参观时段", description = "获取指定日期的可预约参观时段列表")
    public ResponseEntity<List<FamilyVisitSlotDto>> listFamilyVisitSlots(@RequestParam(required = false) LocalDate slotDate) {
        return ResponseEntity.ok(admissionService.listFamilyVisitSlots(slotDate));
    }

    @PostMapping("/family-visit-reservations")
    public ResponseEntity<FamilyVisitReservationDto> createFamilyVisitReservation(@Valid @RequestBody FamilyVisitReservationDto reservationDto) {
        return ResponseEntity.ok(admissionService.createFamilyVisitReservation(reservationDto));
    }

    @GetMapping("/family-visit-reservations/my")
    public ResponseEntity<List<FamilyVisitReservationDto>> listMyFamilyVisitReservations() {
        return ResponseEntity.ok(admissionService.listMyFamilyVisitReservations());
    }

    @GetMapping("/family-visit-reservations")
    public ResponseEntity<List<FamilyVisitReservationDto>> listFamilyVisitReservations(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(admissionService.listFamilyVisitReservations(status));
    }

    @GetMapping("/family-visit-reservations/{reservationId}")
    public ResponseEntity<FamilyVisitReservationDto> getFamilyVisitReservationDetail(@PathVariable Long reservationId) {
        return ResponseEntity.ok(admissionService.getFamilyVisitReservationDetail(reservationId));
    }

    @PostMapping("/family-visit-reservations/{reservationId}/approve")
    public ResponseEntity<FamilyVisitReservationDto> approveFamilyVisitReservation(
            @PathVariable Long reservationId,
            @Valid @RequestBody(required = false) FamilyVisitReservationReviewDto reviewDto) {
        return ResponseEntity.ok(admissionService.approveFamilyVisitReservation(reservationId, reviewDto));
    }

    @PostMapping("/family-visit-reservations/{reservationId}/reject")
    public ResponseEntity<FamilyVisitReservationDto> rejectFamilyVisitReservation(
            @PathVariable Long reservationId,
            @Valid @RequestBody(required = false) FamilyVisitReservationReviewDto reviewDto) {
        return ResponseEntity.ok(admissionService.rejectFamilyVisitReservation(reservationId, reviewDto));
    }

    @PostMapping("/applications")
    public ResponseEntity<ServiceApplicationDto> submitApplication(@Valid @RequestBody ServiceApplicationDto applicationDto) {
        return ResponseEntity.ok(admissionService.submitApplication(applicationDto));
    }

    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<ServiceApplicationDto> getApplication(@PathVariable Long applicationId) {
        return ResponseEntity.ok(admissionService.getApplication(applicationId));
    }

    @GetMapping("/applications")
    public ResponseEntity<List<ServiceApplicationDto>> listApplications(
            @RequestParam(defaultValue = ServiceApplication.STATUS_SUBMITTED) String status) {
        return ResponseEntity.ok(admissionService.listApplicationsByStatus(status));
    }

    @PostMapping("/assessments")
    public ResponseEntity<ServiceApplicationDto> assessEligibility(@Valid @RequestBody EligibilityAssessmentDto assessmentDto) {
        return ResponseEntity.ok(admissionService.assessEligibility(assessmentDto));
    }

    @PostMapping("/applications/{applicationId}/withdraw")
    public ResponseEntity<ServiceApplicationDto> withdrawApplication(
            @PathVariable Long applicationId,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(admissionService.withdrawApplication(applicationId, reason));
    }

    @PostMapping("/applications/{applicationId}/revert-to-assessment")
    public ResponseEntity<ServiceApplicationDto> revertToAssessment(
            @PathVariable Long applicationId,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(admissionService.revertToAssessment(applicationId, reason));
    }
}
