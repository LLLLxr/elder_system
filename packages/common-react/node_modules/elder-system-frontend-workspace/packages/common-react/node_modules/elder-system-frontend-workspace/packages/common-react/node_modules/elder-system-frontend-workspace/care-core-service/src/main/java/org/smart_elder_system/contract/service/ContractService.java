package org.smart_elder_system.contract.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.smart_elder_system.common.dto.care.ServiceAgreementDTO;
import org.smart_elder_system.contract.model.ServiceAgreement;
import org.smart_elder_system.contract.po.ServiceAgreementPo;
import org.smart_elder_system.contract.repository.ServiceAgreementRepository;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ServiceAgreementRepository serviceAgreementRepository;

    public String getModuleScope() {
        return "协议模块：负责服务协议签订、生效、续约与终止";
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceAgreementDTO createDraftAgreement(ServiceAgreementDTO agreementDTO) {
        Optional<ServiceAgreementPo> existing = serviceAgreementRepository.findLatestByApplicationIdForUpdate(agreementDTO.getApplicationId());
        if (existing.isPresent()) {
            return ServiceAgreement.fromPo(existing.get()).toDTO();
        }

        ServiceAgreement domain = ServiceAgreement.fromDTO(agreementDTO);
        domain.setStatus(ServiceAgreement.STATUS_DRAFT);

        ServiceAgreementPo saved = serviceAgreementRepository.save(domain.toPo());
        domain.setAgreementId(saved.getId());
        return domain.toDTO();
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceAgreementDTO signAgreement(ServiceAgreementDTO agreementDTO) {
        ServiceAgreementPo po = serviceAgreementRepository.findByIdForUpdate(agreementDTO.getAgreementId())
                .orElseThrow(() -> new IllegalArgumentException("未找到服务协议"));

        LocalDate effectiveDate = agreementDTO.getEffectiveDate() == null ? LocalDate.now() : agreementDTO.getEffectiveDate();
        LocalDate expiryDate = agreementDTO.getExpiryDate() == null ? effectiveDate.plusYears(1) : agreementDTO.getExpiryDate();
        if (ServiceAgreement.STATUS_ACTIVE.equals(po.getStatus())
                && java.util.Objects.equals(po.getSignedBy(), agreementDTO.getSignedBy())
                && java.util.Objects.equals(po.getEffectiveDate(), effectiveDate)
                && java.util.Objects.equals(po.getExpiryDate(), expiryDate)) {
            return ServiceAgreement.fromPo(po).toDTO();
        }

        ServiceAgreement domain = ServiceAgreement.fromPo(po);
        domain.sign(agreementDTO.getSignedBy(), effectiveDate, expiryDate);

        domain.applyTo(po);
        serviceAgreementRepository.save(po);
        return domain.toDTO();
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceAgreementDTO renewAgreement(Long agreementId, ServiceAgreementDTO agreementDTO) {
        ServiceAgreementPo po = serviceAgreementRepository.findByIdForUpdate(agreementId)
                .orElseThrow(() -> new IllegalArgumentException("未找到服务协议"));

        LocalDate newExpiryDate = agreementDTO.getExpiryDate() == null
                ? LocalDate.now().plusYears(1)
                : agreementDTO.getExpiryDate();
        if (ServiceAgreement.STATUS_RENEWED.equals(po.getStatus())
                && java.util.Objects.equals(po.getExpiryDate(), newExpiryDate)) {
            return ServiceAgreement.fromPo(po).toDTO();
        }

        ServiceAgreement domain = ServiceAgreement.fromPo(po);
        domain.renew(newExpiryDate);

        domain.applyTo(po);
        serviceAgreementRepository.save(po);
        return domain.toDTO();
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceAgreementDTO terminateAgreement(Long agreementId) {
        ServiceAgreementPo po = serviceAgreementRepository.findByIdForUpdate(agreementId)
                .orElseThrow(() -> new IllegalArgumentException("未找到服务协议"));

        if (ServiceAgreement.STATUS_TERMINATED.equals(po.getStatus())) {
            return ServiceAgreement.fromPo(po).toDTO();
        }

        ServiceAgreement domain = ServiceAgreement.fromPo(po);
        domain.terminate();

        domain.applyTo(po);
        serviceAgreementRepository.save(po);
        return domain.toDTO();
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceAgreementDTO revertToDraftAgreement(Long agreementId, String reason) {
        ServiceAgreementPo po = serviceAgreementRepository.findByIdForUpdate(agreementId)
                .orElseThrow(() -> new IllegalArgumentException("未找到服务协议"));

        if (ServiceAgreement.STATUS_DRAFT.equals(po.getStatus())) {
            return ServiceAgreement.fromPo(po).toDTO();
        }

        ServiceAgreement domain = ServiceAgreement.fromPo(po);
        domain.revertToDraft();

        domain.applyTo(po);
        serviceAgreementRepository.save(po);
        return domain.toDTO();
    }

}
