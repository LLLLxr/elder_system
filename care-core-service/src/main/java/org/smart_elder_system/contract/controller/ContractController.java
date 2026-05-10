package org.smart_elder_system.contract.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.smart_elder_system.common.dto.contract.ServiceAgreementDto;
import org.smart_elder_system.contract.service.ContractService;

@RestController
@RequestMapping("/contract")
@RequiredArgsConstructor
@Validated
public class ContractController {

    private final ContractService contractService;

    @GetMapping("/module-scope")
    public ResponseEntity<String> getModuleScope() {
        return ResponseEntity.ok(contractService.getModuleScope());
    }

    @GetMapping("/agreements/{agreementId}")
    public ResponseEntity<ServiceAgreementDto> getAgreement(@PathVariable @Min(1) Long agreementId) {
        return ResponseEntity.ok(contractService.getAgreement(agreementId));
    }

    @GetMapping("/agreements/by-application/{applicationId}")
    public ResponseEntity<ServiceAgreementDto> getLatestAgreementByApplicationId(@PathVariable @Min(1) Long applicationId) {
        return ResponseEntity.ok(contractService.getLatestAgreementByApplicationId(applicationId));
    }

    @PostMapping("/agreements/draft")
    public ResponseEntity<ServiceAgreementDto> createDraftAgreement(@Valid @RequestBody ServiceAgreementDto agreementDto) {
        return ResponseEntity.ok(contractService.createDraftAgreement(agreementDto));
    }

    @PostMapping("/agreements/sign")
    public ResponseEntity<ServiceAgreementDto> signAgreement(@Valid @RequestBody ServiceAgreementDto agreementDto) {
        return ResponseEntity.ok(contractService.signAgreement(agreementDto));
    }

    @PostMapping("/agreements/{agreementId}/renew")
    public ResponseEntity<ServiceAgreementDto> renewAgreement(
            @PathVariable @Min(1) Long agreementId,
            @Valid @RequestBody ServiceAgreementDto agreementDto) {

        return ResponseEntity.ok(contractService.renewAgreement(agreementId, agreementDto));
    }

    @PostMapping("/agreements/{agreementId}/terminate")
    public ResponseEntity<ServiceAgreementDto> terminateAgreement(@PathVariable @Min(1) Long agreementId) {
        return ResponseEntity.ok(contractService.terminateAgreement(agreementId));
    }

    @PostMapping("/agreements/{agreementId}/revert-to-draft")
    public ResponseEntity<ServiceAgreementDto> revertToDraftAgreement(
            @PathVariable @Min(1) Long agreementId,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(contractService.revertToDraftAgreement(agreementId, reason));
    }
}
