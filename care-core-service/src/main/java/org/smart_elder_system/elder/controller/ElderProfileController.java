package org.smart_elder_system.elder.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.smart_elder_system.common.dto.elder.ElderProfileDto;
import org.smart_elder_system.elder.service.ElderProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/elders")
@Validated
public class ElderProfileController {

    private final ElderProfileService elderProfileService;

    public ElderProfileController(ElderProfileService elderProfileService) {
        this.elderProfileService = elderProfileService;
    }

    @GetMapping("/{elderId}")
    public ResponseEntity<ElderProfileDto> getByElderId(@PathVariable @Min(1) Long elderId) {
        return ResponseEntity.ok(elderProfileService.getByElderId(elderId));
    }

    @GetMapping(params = "idCard")
    public ResponseEntity<ElderProfileDto> getByIdCard(@RequestParam @NotBlank String idCard) {
        return ResponseEntity.ok(elderProfileService.getByIdCard(idCard));
    }

    @PostMapping
    public ResponseEntity<ElderProfileDto> createIfAbsent(@Valid @RequestBody ElderProfileDto dto) {
        return ResponseEntity.ok(elderProfileService.createIfAbsent(dto));
    }
}
