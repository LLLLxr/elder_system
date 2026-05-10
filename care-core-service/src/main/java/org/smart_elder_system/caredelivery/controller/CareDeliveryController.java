package org.smart_elder_system.caredelivery.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.smart_elder_system.caredelivery.service.CareDeliveryService;
import org.smart_elder_system.common.dto.caredelivery.CarePlanDto;
import org.smart_elder_system.common.dto.caredelivery.CaregiverCheckInRecordDto;
import org.smart_elder_system.common.dto.caredelivery.CaregiverCheckInSubmitDto;
import org.smart_elder_system.common.dto.caredelivery.DailyCareTaskDto;
import org.smart_elder_system.common.dto.caredelivery.FamilyServicePlanDto;
import org.smart_elder_system.common.dto.caredelivery.NurseCareRecordDto;
import org.smart_elder_system.common.dto.caredelivery.NurseCareRecordSaveDto;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/care-delivery")
@RequiredArgsConstructor
@Validated
public class CareDeliveryController {

    private final CareDeliveryService careDeliveryService;

    @GetMapping("/module-scope")
    public ResponseEntity<String> getModuleScope() {
        return ResponseEntity.ok(careDeliveryService.getModuleScope());
    }

    @PostMapping("/plans")
    public ResponseEntity<CarePlanDto> createCarePlan(@Valid @RequestBody CarePlanDto carePlanDto) {
        return ResponseEntity.ok(careDeliveryService.createCarePlan(carePlanDto));
    }

    @PostMapping("/plans/{planId}/start")
    public ResponseEntity<CarePlanDto> startCarePlan(@PathVariable @Min(1) Long planId) {
        return ResponseEntity.ok(careDeliveryService.startCarePlan(planId));
    }

    @PostMapping("/plans/{planId}/close")
    public ResponseEntity<CarePlanDto> closeCarePlan(@PathVariable @Min(1) Long planId) {
        return ResponseEntity.ok(careDeliveryService.closeCarePlan(planId));
    }

    @GetMapping("/my-tasks")
    public ResponseEntity<List<DailyCareTaskDto>> listMyTasks(
            @RequestParam LocalDate taskDate,
            @RequestParam(required = false) @Min(1) Long elderId) {
        return ResponseEntity.ok(careDeliveryService.listMyDailyTasks(taskDate, elderId));
    }

    @PostMapping("/my-tasks/{servicePlanId}/check-in")
    public ResponseEntity<CaregiverCheckInRecordDto> submitCheckIn(
            @PathVariable @Min(1) Long servicePlanId,
            @Valid @RequestBody CaregiverCheckInSubmitDto dto) {
        return ResponseEntity.ok(careDeliveryService.submitCheckIn(servicePlanId, dto));
    }

    @GetMapping("/my-check-ins")
    public ResponseEntity<List<CaregiverCheckInRecordDto>> listMyCheckIns(
            @RequestParam(required = false) @Min(1) Long elderId,
            @RequestParam(required = false) LocalDate taskDate) {
        return ResponseEntity.ok(careDeliveryService.listMyCheckIns(elderId, taskDate));
    }

    @GetMapping("/nurse-care-records")
    public ResponseEntity<List<NurseCareRecordDto>> listNurseCareRecords(
            @RequestParam(required = false) @Min(1) Long elderId,
            @RequestParam(required = false) @Min(1) Long nurseId,
            @RequestParam(required = false) LocalDate recordDate) {
        return ResponseEntity.ok(careDeliveryService.listNurseCareRecords(elderId, nurseId, recordDate));
    }

    @PostMapping("/nurse-care-records")
    public ResponseEntity<NurseCareRecordDto> createNurseCareRecord(@Valid @RequestBody NurseCareRecordSaveDto dto) {
        return ResponseEntity.ok(careDeliveryService.createNurseCareRecord(dto));
    }

    @PutMapping("/nurse-care-records/{recordId}")
    public ResponseEntity<NurseCareRecordDto> updateNurseCareRecord(
            @PathVariable @Min(1) Long recordId,
            @Valid @RequestBody NurseCareRecordSaveDto dto) {
        return ResponseEntity.ok(careDeliveryService.updateNurseCareRecord(recordId, dto));
    }

    @GetMapping("/nurse-care-records/{recordId}")
    public ResponseEntity<NurseCareRecordDto> getNurseCareRecord(@PathVariable @Min(1) Long recordId) {
        return ResponseEntity.ok(careDeliveryService.getNurseCareRecord(recordId));
    }

    @GetMapping("/family/service-plans/{elderId}")
    public ResponseEntity<List<FamilyServicePlanDto>> listFamilyServicePlans(@PathVariable @Min(1) Long elderId) {
        return ResponseEntity.ok(careDeliveryService.listFamilyServicePlans(elderId));
    }

    @GetMapping("/family/check-ins/{elderId}")
    public ResponseEntity<List<CaregiverCheckInRecordDto>> listFamilyCheckIns(
            @PathVariable @Min(1) Long elderId,
            @RequestParam(required = false) LocalDate taskDate) {
        return ResponseEntity.ok(careDeliveryService.listFamilyCheckIns(elderId, taskDate));
    }

    @GetMapping("/family/nurse-care-records/{elderId}")
    public ResponseEntity<List<NurseCareRecordDto>> listFamilyNurseCareRecords(
            @PathVariable @Min(1) Long elderId,
            @RequestParam(required = false) LocalDate recordDate) {
        return ResponseEntity.ok(careDeliveryService.listFamilyNurseCareRecords(elderId, recordDate));
    }
}
