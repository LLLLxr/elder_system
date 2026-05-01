package org.smart_elder_system.resourcescheduling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.smart_elder_system.resourcescheduling.service.ResourceSchedulingService;

@RestController
@RequestMapping("/resource-scheduling")
@RequiredArgsConstructor
public class ResourceSchedulingController {

    private final ResourceSchedulingService resourceSchedulingService;

    @GetMapping("/module-scope")
    public ResponseEntity<String> getModuleScope() {
        return ResponseEntity.ok(resourceSchedulingService.getModuleScope());
    }
}
