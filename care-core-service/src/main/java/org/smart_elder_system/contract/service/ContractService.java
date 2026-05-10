package org.smart_elder_system.contract.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.smart_elder_system.common.dto.contract.ServiceAgreementDto;
import org.smart_elder_system.contract.vo.ServiceAgreement;
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

    public ServiceAgreementDto getAgreement(Long agreementId) {
        ServiceAgreementPo po = serviceAgreementRepository.findById(agreementId)
                .orElseThrow(() -> new IllegalArgumentException("未找到服务协议"));
        return ServiceAgreement.fromPo(po).toDto();
    }

    public ServiceAgreementDto getLatestAgreementByApplicationId(Long applicationId) {
        ServiceAgreementPo po = serviceAgreementRepository.findTopByApplicationIdOrderByIdDesc(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("当前申请暂无服务协议"));
        return ServiceAgreement.fromPo(po).toDto();
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceAgreementDto createDraftAgreement(ServiceAgreementDto agreementDto) {
        Optional<ServiceAgreementPo> existing = serviceAgreementRepository.findLatestByApplicationIdForUpdate(agreementDto.getApplicationId());
        if (existing.isPresent()) {
            return ServiceAgreement.fromPo(existing.get()).toDto();
        }

        ServiceAgreement domain = ServiceAgreement.fromDto(agreementDto);
        domain.setStatus(ServiceAgreement.STATUS_DRAFT);

        ServiceAgreementPo saved = serviceAgreementRepository.save(domain.toPo());
        domain.setAgreementId(saved.getId());
        return domain.toDto();
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceAgreementDto signAgreement(ServiceAgreementDto agreementDto) {
        ServiceAgreementPo po = serviceAgreementRepository.findByIdForUpdate(agreementDto.getAgreementId())
                .orElseThrow(() -> new IllegalArgumentException("未找到服务协议"));

        LocalDate effectiveDate = agreementDto.getEffectiveDate() == null ? LocalDate.now() : agreementDto.getEffectiveDate();
        LocalDate expiryDate = agreementDto.getExpiryDate() == null ? effectiveDate.plusMonths(1) : agreementDto.getExpiryDate();
        if (ServiceAgreement.STATUS_ACTIVE.equals(po.getStatus())
                && java.util.Objects.equals(po.getSignedBy(), agreementDto.getSignedBy())
                && java.util.Objects.equals(po.getEffectiveDate(), effectiveDate)
                && java.util.Objects.equals(po.getExpiryDate(), expiryDate)) {
            return ServiceAgreement.fromPo(po).toDto();
        }

        ServiceAgreement domain = ServiceAgreement.fromPo(po);
        domain.sign(agreementDto.getSignedBy(), effectiveDate, expiryDate);

        domain.applyTo(po);
        serviceAgreementRepository.save(po);
        return domain.toDto();
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceAgreementDto renewAgreement(Long agreementId, ServiceAgreementDto agreementDto) {
        ServiceAgreementPo po = serviceAgreementRepository.findByIdForUpdate(agreementId)
                .orElseThrow(() -> new IllegalArgumentException("未找到服务协议"));

        LocalDate currentExpiryDate = po.getExpiryDate() == null ? LocalDate.now() : po.getExpiryDate();
        LocalDate newExpiryDate = agreementDto.getExpiryDate() == null
                ? currentExpiryDate.plusMonths(1)
                : agreementDto.getExpiryDate();
        if (ServiceAgreement.STATUS_ACTIVE.equals(po.getStatus())
                && java.util.Objects.equals(po.getExpiryDate(), newExpiryDate)) {
            return ServiceAgreement.fromPo(po).toDto();
        }

        ServiceAgreement domain = ServiceAgreement.fromPo(po);
        domain.renew(newExpiryDate);

        domain.applyTo(po);
        serviceAgreementRepository.save(po);
        return domain.toDto();
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceAgreementDto terminateAgreement(Long agreementId) {
        ServiceAgreementPo po = serviceAgreementRepository.findByIdForUpdate(agreementId)
                .orElseThrow(() -> new IllegalArgumentException("未找到服务协议"));

        if (ServiceAgreement.STATUS_TERMINATED.equals(po.getStatus())) {
            return ServiceAgreement.fromPo(po).toDto();
        }

        ServiceAgreement domain = ServiceAgreement.fromPo(po);
        domain.terminate();

        domain.applyTo(po);
        serviceAgreementRepository.save(po);
        return domain.toDto();
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceAgreementDto revertToDraftAgreement(Long agreementId, String reason) {
        ServiceAgreementPo po = serviceAgreementRepository.findByIdForUpdate(agreementId)
                .orElseThrow(() -> new IllegalArgumentException("未找到服务协议"));

        if (ServiceAgreement.STATUS_DRAFT.equals(po.getStatus())) {
            return ServiceAgreement.fromPo(po).toDto();
        }

        ServiceAgreement domain = ServiceAgreement.fromPo(po);
        domain.revertToDraft();

        domain.applyTo(po);
        serviceAgreementRepository.save(po);
        return domain.toDto();
    }

}
