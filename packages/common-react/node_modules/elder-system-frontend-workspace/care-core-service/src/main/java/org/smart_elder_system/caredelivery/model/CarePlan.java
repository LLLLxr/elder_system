package org.smart_elder_system.caredelivery.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.smart_elder_system.caredelivery.po.CarePlanPo;
import org.smart_elder_system.common.dto.care.CarePlanDTO;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarePlan {

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_CLOSED = "CLOSED";

    private Long planId;
    private Long agreementId;
    private Long elderId;
    private String planName;
    private String serviceScene;
    private String personalizationNote;
    private String status;
    private LocalDate planDate;

    public static CarePlan fromDTO(CarePlanDTO dto) {
        return CarePlan.builder()
                .planId(dto.getPlanId())
                .agreementId(dto.getAgreementId())
                .elderId(dto.getElderId())
                .planName(dto.getPlanName())
                .serviceScene(dto.getServiceScene())
                .personalizationNote(dto.getPersonalizationNote())
                .status(dto.getStatus())
                .planDate(dto.getPlanDate())
                .build();
    }

    public static CarePlan fromPo(CarePlanPo po) {
        return CarePlan.builder()
                .planId(po.getId())
                .agreementId(po.getAgreementId())
                .elderId(po.getElderId())
                .planName(po.getPlanName())
                .serviceScene(po.getServiceScene())
                .personalizationNote(po.getPersonalizationNote())
                .status(po.getStatus())
                .planDate(po.getPlanDate())
                .build();
    }

    public CarePlanDTO toDTO() {
        return CarePlanDTO.builder()
                .planId(planId)
                .agreementId(agreementId)
                .elderId(elderId)
                .planName(planName)
                .serviceScene(serviceScene)
                .personalizationNote(personalizationNote)
                .status(status)
                .planDate(planDate)
                .build();
    }

    public CarePlanPo toPo() {
        return CarePlanPo.builder()
                .id(planId)
                .agreementId(agreementId)
                .elderId(elderId)
                .planName(planName)
                .serviceScene(serviceScene)
                .personalizationNote(personalizationNote)
                .status(status)
                .planDate(planDate)
                .build();
    }

    public void applyTo(CarePlanPo po) {
        po.setId(planId);
        po.setAgreementId(agreementId);
        po.setElderId(elderId);
        po.setPlanName(planName);
        po.setServiceScene(serviceScene);
        po.setPersonalizationNote(personalizationNote);
        po.setStatus(status);
        po.setPlanDate(planDate);
    }

    public void create() {
        this.status = STATUS_CREATED;
        this.planDate = LocalDate.now();
    }

    public void start() {
        this.status = STATUS_IN_PROGRESS;
    }

    public void close() {
        this.status = STATUS_CLOSED;
    }
}
