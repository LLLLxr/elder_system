package org.smart_elder_system.careorchestration.feign;

import org.smart_elder_system.common.dto.care.ServiceAgreementDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "contract", url = "${services.contract.url}", path = "/contract")
public interface ContractClient {

    @PostMapping("/agreements/draft")
    ServiceAgreementDTO createDraftAgreement(@RequestBody ServiceAgreementDTO agreementDTO);

    @PostMapping("/agreements/sign")
    ServiceAgreementDTO signAgreement(@RequestBody ServiceAgreementDTO agreementDTO);

    @PostMapping("/agreements/{agreementId}/renew")
    ServiceAgreementDTO renewAgreement(@PathVariable("agreementId") Long agreementId,
                                       @RequestBody ServiceAgreementDTO agreementDTO);

    @PostMapping("/agreements/{agreementId}/terminate")
    ServiceAgreementDTO terminateAgreement(@PathVariable("agreementId") Long agreementId);

    @PostMapping("/agreements/{agreementId}/revert-to-draft")
    ServiceAgreementDTO revertToDraftAgreement(@PathVariable("agreementId") Long agreementId,
                                               @RequestParam(value = "reason", required = false) String reason);
}
