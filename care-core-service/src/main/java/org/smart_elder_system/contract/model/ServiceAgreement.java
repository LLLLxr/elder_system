package org.smart_elder_system.contract.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.smart_elder_system.common.dto.care.ServiceAgreementDTO;
import org.smart_elder_system.contract.po.ServiceAgreementPo;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceAgreement {

    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_EXPIRED = "EXPIRED";
    public static final String STATUS_TERMINATED = "TERMINATED";
    public static final String STATUS_RENEWED = "RENEWED";

    private Long agreementId;
    private Long applicationId;
    private Long elderId;
    private String serviceScene;
    private String status;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;
    private String signedBy;

    public static ServiceAgreement fromDTO(ServiceAgreementDTO dto) {
        return ServiceAgreement.builder()
                .agreementId(dto.getAgreementId())
                .applicationId(dto.getApplicationId())
                .elderId(dto.getElderId())
                .serviceScene(dto.getServiceScene())
                .status(dto.getStatus())
                .effectiveDate(dto.getEffectiveDate())
                .expiryDate(dto.getExpiryDate())
                .signedBy(dto.getSignedBy())
                .build();
    }

    public static ServiceAgreement fromPo(ServiceAgreementPo po) {
        return ServiceAgreement.builder()
                .agreementId(po.getId())
                .applicationId(po.getApplicationId())
                .elderId(po.getElderId())
                .serviceScene(po.getServiceScene())
                .status(po.getStatus())
                .effectiveDate(po.getEffectiveDate())
                .expiryDate(po.getExpiryDate())
                .signedBy(po.getSignedBy())
                .build();
    }

    public ServiceAgreementDTO toDTO() {
        return ServiceAgreementDTO.builder()
                .agreementId(agreementId)
                .applicationId(applicationId)
                .elderId(elderId)
                .serviceScene(serviceScene)
                .status(status)
                .effectiveDate(effectiveDate)
                .expiryDate(expiryDate)
                .signedBy(signedBy)
                .build();
    }

    public ServiceAgreementPo toPo() {
        return ServiceAgreementPo.builder()
                .id(agreementId)
                .applicationId(applicationId)
                .elderId(elderId)
                .serviceScene(serviceScene)
                .status(status)
                .effectiveDate(effectiveDate)
                .expiryDate(expiryDate)
                .signedBy(signedBy)
                .build();
    }

    public void applyTo(ServiceAgreementPo po) {
        po.setId(agreementId);
        po.setApplicationId(applicationId);
        po.setElderId(elderId);
        po.setServiceScene(serviceScene);
        po.setStatus(status);
        po.setEffectiveDate(effectiveDate);
        po.setExpiryDate(expiryDate);
        po.setSignedBy(signedBy);
    }

    public void sign(String signer, LocalDate effectiveDate, LocalDate expiryDate) {
        this.signedBy = signer;
        this.effectiveDate = effectiveDate;
        this.expiryDate = expiryDate;
        this.status = STATUS_ACTIVE;
    }

    public void terminate() {
        this.status = STATUS_TERMINATED;
    }

    public void expire() {
        this.status = STATUS_EXPIRED;
    }

    public void renew(LocalDate newExpiryDate) {
        this.expiryDate = newExpiryDate;
        this.status = STATUS_RENEWED;
    }

    public void revertToDraft() {
        if (!STATUS_ACTIVE.equals(this.status) && !STATUS_DRAFT.equals(this.status)) {
            throw new IllegalStateException("当前协议状态不允许退回待签约");
        }
        this.status = STATUS_DRAFT;
        this.effectiveDate = null;
        this.expiryDate = null;
        this.signedBy = null;
    }
}
