package org.smart_elder_system.quality.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.smart_elder_system.common.dto.care.ServiceReviewDTO;
import org.smart_elder_system.quality.service.QualityService;

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
    public ResponseEntity<ServiceReviewDTO> reviewService(@Valid @RequestBody ServiceReviewDTO reviewDTO) {
        return ResponseEntity.ok(qualityService.reviewService(reviewDTO));
    }
}
