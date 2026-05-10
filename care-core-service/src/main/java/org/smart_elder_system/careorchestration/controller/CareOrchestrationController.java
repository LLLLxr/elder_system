package org.smart_elder_system.careorchestration.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.smart_elder_system.careorchestration.dto.CareAnalyticsOverviewDto;
import org.smart_elder_system.careorchestration.dto.CareAnalyticsTrendsDto;
import org.smart_elder_system.careorchestration.dto.PagedResponseDto;
import org.smart_elder_system.careorchestration.dto.ServiceJourneyTaskItemDto;
import org.smart_elder_system.careorchestration.dto.ServiceJourneyTaskOverviewDto;
import org.smart_elder_system.careorchestration.dto.ServiceJourneyTransitionLogItemDto;
import org.smart_elder_system.careorchestration.journey.ServiceJourneyState;
import org.smart_elder_system.careorchestration.service.CareOrchestrationService;
import org.smart_elder_system.common.dto.admission.IntakeRecordDto;
import org.smart_elder_system.common.dto.contract.RenewalContextDto;
import org.smart_elder_system.common.dto.careorchestration.ServiceJourneyResultDto;

import java.util.List;

@RestController
@RequestMapping("/care-orchestration")
@RequiredArgsConstructor
@Validated
public class CareOrchestrationController {

    private final CareOrchestrationService careOrchestrationService;

    @GetMapping("/journey-overview")
    public ResponseEntity<String> getJourneyOverview() {
        return ResponseEntity.ok(careOrchestrationService.getServiceJourneyOverview());
    }

    @PostMapping("/journeys/start")
    public ResponseEntity<ServiceJourneyResultDto> startServiceJourney(
            @RequestParam @Min(1) Long elderId,
            @RequestParam(required = false) @Min(1) Long guardianId,
            @RequestParam @NotBlank String applicantName,
            @RequestParam @NotBlank String contactPhone,
            @RequestParam @NotBlank String serviceScene,
            @RequestParam @NotBlank String serviceRequest) {

        ServiceJourneyResultDto result = careOrchestrationService.startServiceJourney(
                elderId,
                guardianId,
                applicantName,
                contactPhone,
                serviceScene,
                serviceRequest);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/journeys/continue")
    public ResponseEntity<ServiceJourneyResultDto> continueAfterAssessment(
            @RequestParam @Min(1) Long applicationId) {
        return ResponseEntity.ok(careOrchestrationService.continueAfterAssessment(applicationId));
    }

    @PostMapping("/journeys/reject-admission")
    public ResponseEntity<ServiceJourneyResultDto> rejectAdmissionJourney(
            @RequestParam @Min(1) Long applicationId,
            @RequestParam @NotBlank String assessmentConclusion,
            @RequestParam @NotBlank String assessor) {
        return ResponseEntity.ok(careOrchestrationService.rejectAdmissionJourney(applicationId, assessmentConclusion, assessor));
    }

    @PostMapping("/journeys/reject-health")
    public ResponseEntity<ServiceJourneyResultDto> rejectHealthJourney(
            @RequestParam @Min(1) Long applicationId,
            @RequestParam @NotBlank String assessmentConclusion,
            @RequestParam @NotBlank String assessor,
            @RequestParam @NotBlank String responsibleDoctor,
            @RequestParam Integer score) {
        return ResponseEntity.ok(careOrchestrationService.rejectHealthJourney(
                applicationId,
                assessmentConclusion,
                assessor,
                responsibleDoctor,
                score));
    }

    @PostMapping("/journeys/withdraw")
    public ResponseEntity<ServiceJourneyResultDto> withdrawServiceJourney(
            @RequestParam @Min(1) Long applicationId,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(careOrchestrationService.withdrawServiceJourney(applicationId, reason));
    }

    @PostMapping("/journeys/review")
    public ResponseEntity<ServiceJourneyResultDto> reviewAndFinalize(
            @RequestParam @Min(1) Long agreementId,
            @RequestParam @Min(1) Long elderId,
            @RequestParam @Min(0) Integer satisfactionScore,
            @RequestParam(required = false) String reviewComment) {

        ServiceJourneyResultDto result = careOrchestrationService.reviewAndFinalize(
                agreementId,
                elderId,
                satisfactionScore,
                reviewComment);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/renewal-context/latest/by-applicant")
    public ResponseEntity<RenewalContextDto> getLatestRenewalContextByApplicant(
            @RequestParam @NotBlank String applicantName) {
        return ResponseEntity.ok(careOrchestrationService.getLatestRenewalContextByApplicant(applicantName));
    }

    @PostMapping("/renewals/review")
    public ResponseEntity<RenewalContextDto> submitRenewalReview(
            @RequestParam @Min(1) Long agreementId,
            @RequestParam @Min(1) Long elderId,
            @RequestParam @Min(0) Integer satisfactionScore,
            @RequestParam(required = false) String reviewComment) {
        return ResponseEntity.ok(careOrchestrationService.submitRenewalReview(
                agreementId,
                elderId,
                satisfactionScore,
                reviewComment));
    }

    @PostMapping("/renewals/{agreementId}/confirm")
    public ResponseEntity<RenewalContextDto> confirmRenewal(
            @PathVariable @Min(1) Long agreementId,
            @RequestParam @Min(1) @Max(12) Integer renewMonths) {
        if (renewMonths == null || renewMonths < 1 || renewMonths > 12) {
            throw new IllegalArgumentException("续约月数必须在1到12个月之间");
        }
        return ResponseEntity.ok(careOrchestrationService.confirmRenewal(agreementId, renewMonths));
    }

    @PostMapping("/renewals/{agreementId}/decline")
    public ResponseEntity<RenewalContextDto> declineRenewal(
            @PathVariable @Min(1) Long agreementId,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(careOrchestrationService.declineRenewal(agreementId, reason));
    }

    @GetMapping("/journeys/intake-records")
    public ResponseEntity<List<IntakeRecordDto>> listIntakeRecords(
            @RequestParam @Min(1) Long elderId) {
        return ResponseEntity.ok(careOrchestrationService.listIntakeRecords(elderId));
    }

    @GetMapping("/journeys/intake-records/by-applicant")
    public ResponseEntity<List<IntakeRecordDto>> listIntakeRecordsByApplicant(
            @RequestParam @NotBlank String applicantName) {
        return ResponseEntity.ok(careOrchestrationService.listIntakeRecordsByApplicant(applicantName));
    }

    @GetMapping("/journeys/latest-result/by-applicant")
    public ResponseEntity<ServiceJourneyResultDto> getLatestJourneyResultByApplicant(
            @RequestParam @NotBlank String applicantName) {
        return ResponseEntity.ok(careOrchestrationService.getLatestJourneyResultByApplicant(applicantName));
    }

    @GetMapping("/analytics/overview")
    public ResponseEntity<CareAnalyticsOverviewDto> getAnalyticsOverview() {
        return ResponseEntity.ok(careOrchestrationService.getAnalyticsOverview());
    }

    @GetMapping("/journey-tasks")
    public ResponseEntity<PagedResponseDto<ServiceJourneyTaskItemDto>> listJourneyTasks(
            @RequestParam(required = false) @Min(1) Long applicationId,
            @RequestParam(required = false) @Min(1) Long elderId,
            @RequestParam(required = false) @Min(1) Long agreementId,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) String assigneeRole,
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) Integer size,
            @RequestParam(defaultValue = "dueAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {
        return ResponseEntity.ok(PagedResponseDto.from(careOrchestrationService.listJourneyTasks(
                applicationId,
                elderId,
                agreementId,
                taskType,
                statuses,
                assigneeRole,
                page,
                size,
                sortBy,
                sortOrder)));
    }

    @GetMapping("/journey-tasks/timeline")
    public ResponseEntity<List<ServiceJourneyTaskItemDto>> listJourneyTaskTimeline(
            @RequestParam @Min(1) Long applicationId) {
        return ResponseEntity.ok(careOrchestrationService.listJourneyTaskTimeline(applicationId));
    }

    @GetMapping("/journey-tasks/overview")
    public ResponseEntity<ServiceJourneyTaskOverviewDto> getJourneyTaskOverview(
            @RequestParam(required = false) @Min(1) Long applicationId,
            @RequestParam(required = false) @Min(1) Long elderId,
            @RequestParam(required = false) @Min(1) Long agreementId,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) String assigneeRole) {
        return ResponseEntity.ok(careOrchestrationService.getJourneyTaskOverview(
                applicationId,
                elderId,
                agreementId,
                taskType,
                statuses,
                assigneeRole));
    }

    @GetMapping("/journey-transition-logs/by-application")
    public ResponseEntity<List<ServiceJourneyTransitionLogItemDto>> listJourneyTransitionLogsByApplication(
            @RequestParam @Min(1) Long applicationId) {
        return ResponseEntity.ok(careOrchestrationService.listJourneyTransitionLogsByApplication(applicationId));
    }

    @GetMapping("/journey-transition-logs/by-agreement")
    public ResponseEntity<List<ServiceJourneyTransitionLogItemDto>> listJourneyTransitionLogsByAgreement(
            @RequestParam @Min(1) Long agreementId) {
        return ResponseEntity.ok(careOrchestrationService.listJourneyTransitionLogsByAgreement(agreementId));
    }

    @PostMapping("/journeys/return")
    public ResponseEntity<ServiceJourneyResultDto> returnJourneyStep(
            @RequestParam @Min(1) Long applicationId,
            @RequestParam ServiceJourneyState targetState,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(careOrchestrationService.returnJourneyStep(applicationId, targetState, reason));
    }

    @GetMapping("/analytics/trends")
    public ResponseEntity<CareAnalyticsTrendsDto> getAnalyticsTrends(
            @RequestParam(defaultValue = "30") @Min(1) Integer days) {
        return ResponseEntity.ok(careOrchestrationService.getAnalyticsTrends(days));
    }
}
