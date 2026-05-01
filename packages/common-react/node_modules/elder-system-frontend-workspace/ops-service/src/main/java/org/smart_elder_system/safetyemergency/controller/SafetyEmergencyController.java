package org.smart_elder_system.safetyemergency.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.smart_elder_system.safetyemergency.service.SafetyEmergencyService;

@RestController
@RequestMapping("/safety-emergency")
@RequiredArgsConstructor
public class SafetyEmergencyController {

    private final SafetyEmergencyService safetyEmergencyService;

    @GetMapping("/module-scope")
    public ResponseEntity<String> getModuleScope() {
        return ResponseEntity.ok(safetyEmergencyService.getModuleScope());
    }
}
