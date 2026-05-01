package org.smart_elder_system.caredelivery.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.smart_elder_system.caredelivery.service.CareDeliveryService;
import org.smart_elder_system.common.dto.care.CarePlanDTO;

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
    public ResponseEntity<CarePlanDTO> createCarePlan(@Valid @RequestBody CarePlanDTO carePlanDTO) {
        return ResponseEntity.ok(careDeliveryService.createCarePlan(carePlanDTO));
    }

    @PostMapping("/plans/{planId}/start")
    public ResponseEntity<CarePlanDTO> startCarePlan(@PathVariable @Min(1) Long planId) {
        return ResponseEntity.ok(careDeliveryService.startCarePlan(planId));
    }

    @PostMapping("/plans/{planId}/close")
    public ResponseEntity<CarePlanDTO> closeCarePlan(@PathVariable @Min(1) Long planId) {
        return ResponseEntity.ok(careDeliveryService.closeCarePlan(planId));
    }
}
