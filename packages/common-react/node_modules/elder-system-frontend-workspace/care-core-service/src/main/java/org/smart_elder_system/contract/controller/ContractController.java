package org.smart_elder_system.contract.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.smart_elder_system.common.dto.care.ServiceAgreementDTO;
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

    @PostMapping("/agreements/draft")
    public ResponseEntity<ServiceAgreementDTO> createDraftAgreement(@Valid @RequestBody ServiceAgreementDTO agreementDTO) {
        return ResponseEntity.ok(contractService.createDraftAgreement(agreementDTO));
    }

    @PostMapping("/agreements/sign")
    public ResponseEntity<ServiceAgreementDTO> signAgreement(@Valid @RequestBody ServiceAgreementDTO agreementDTO) {
        return ResponseEntity.ok(contractService.signAgreement(agreementDTO));
    }

    @PostMapping("/agreements/{agreementId}/renew")
    public ResponseEntity<ServiceAgreementDTO> renewAgreement(
            @PathVariable @Min(1) Long agreementId,
            @Valid @RequestBody ServiceAgreementDTO agreementDTO) {

        return ResponseEntity.ok(contractService.renewAgreement(agreementId, agreementDTO));
    }

    @PostMapping("/agreements/{agreementId}/terminate")
    public ResponseEntity<ServiceAgreementDTO> terminateAgreement(@PathVariable @Min(1) Long agreementId) {
        return ResponseEntity.ok(contractService.terminateAgreement(agreementId));
    }

    @PostMapping("/agreements/{agreementId}/revert-to-draft")
    public ResponseEntity<ServiceAgreementDTO> revertToDraftAgreement(
            @PathVariable @Min(1) Long agreementId,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(contractService.revertToDraftAgreement(agreementId, reason));
    }
}
