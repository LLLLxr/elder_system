package org.smart_elder_system.careorchestration.feign;

import org.smart_elder_system.common.dto.contract.ServiceAgreementDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "contract", url = "${services.contract.url}", path = "/contract")
public interface ContractClient {

    @PostMapping("/agreements/draft")
    ServiceAgreementDto createDraftAgreement(@RequestBody ServiceAgreementDto agreementDto);

    @PostMapping("/agreements/sign")
    ServiceAgreementDto signAgreement(@RequestBody ServiceAgreementDto agreementDto);

    @PostMapping("/agreements/{agreementId}/renew")
    ServiceAgreementDto renewAgreement(@PathVariable("agreementId") Long agreementId,
                                       @RequestBody ServiceAgreementDto agreementDto);

    @PostMapping("/agreements/{agreementId}/terminate")
    ServiceAgreementDto terminateAgreement(@PathVariable("agreementId") Long agreementId);

    @PostMapping("/agreements/{agreementId}/revert-to-draft")
    ServiceAgreementDto revertToDraftAgreement(@PathVariable("agreementId") Long agreementId,
                                               @RequestParam(value = "reason", required = false) String reason);
}
