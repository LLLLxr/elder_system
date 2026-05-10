package org.smart_elder_system.admission.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.smart_elder_system.admission.po.FamilyVisitReservationPo;
import org.smart_elder_system.common.dto.admission.FamilyVisitReservationDto;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyVisitReservation {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private Long reservationId;
    private Long slotId;
    private Long elderId;
    private Long familyUserId;
    private String familyUsername;
    private String visitorName;
    private String visitorPhone;
    private String relationToElder;
    private String visitPurpose;
    private String status;
    private String reviewedBy;
    private String reviewComment;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;

    public static FamilyVisitReservation fromDto(FamilyVisitReservationDto dto) {
        return FamilyVisitReservation.builder()
                .reservationId(dto.getReservationId())
                .slotId(dto.getSlotId())
                .elderId(dto.getElderId())
                .familyUserId(dto.getFamilyUserId())
                .familyUsername(dto.getFamilyUsername())
                .visitorName(dto.getVisitorName())
                .visitorPhone(dto.getVisitorPhone())
                .relationToElder(dto.getRelationToElder())
                .visitPurpose(dto.getVisitPurpose())
                .status(dto.getStatus())
                .reviewedBy(dto.getReviewedBy())
                .reviewComment(dto.getReviewComment())
                .reviewedAt(dto.getReviewedAt())
                .createdAt(dto.getCreatedAt())
                .build();
    }

    public static FamilyVisitReservation fromPo(FamilyVisitReservationPo po) {
        return FamilyVisitReservation.builder()
                .reservationId(po.getId())
                .slotId(po.getSlotId())
                .elderId(po.getElderId())
                .familyUserId(po.getFamilyUserId())
                .familyUsername(po.getFamilyUsername())
                .visitorName(po.getVisitorName())
                .visitorPhone(po.getVisitorPhone())
                .relationToElder(po.getRelationToElder())
                .visitPurpose(po.getVisitPurpose())
                .status(po.getStatus())
                .reviewedBy(po.getReviewedBy())
                .reviewComment(po.getReviewComment())
                .reviewedAt(po.getReviewedAt())
                .createdAt(po.getCreatedDateTimeUtc())
                .build();
    }

    public FamilyVisitReservationDto toDto() {
        return FamilyVisitReservationDto.builder()
                .reservationId(reservationId)
                .slotId(slotId)
                .elderId(elderId)
                .familyUserId(familyUserId)
                .familyUsername(familyUsername)
                .visitorName(visitorName)
                .visitorPhone(visitorPhone)
                .relationToElder(relationToElder)
                .visitPurpose(visitPurpose)
                .status(status)
                .reviewedBy(reviewedBy)
                .reviewComment(reviewComment)
                .reviewedAt(reviewedAt)
                .createdAt(createdAt)
                .build();
    }

    public void submit() {
        this.status = STATUS_PENDING;
    }

    public void approve(String reviewer, String comment) {
        ensurePending();
        this.status = STATUS_APPROVED;
        this.reviewedBy = reviewer;
        this.reviewComment = comment;
        this.reviewedAt = LocalDateTime.now();
    }

    public void reject(String reviewer, String comment) {
        ensurePending();
        this.status = STATUS_REJECTED;
        this.reviewedBy = reviewer;
        this.reviewComment = comment;
        this.reviewedAt = LocalDateTime.now();
    }

    public void applyTo(FamilyVisitReservationPo po) {
        po.setId(reservationId);
        po.setSlotId(slotId);
        po.setElderId(elderId);
        po.setFamilyUserId(familyUserId);
        po.setFamilyUsername(familyUsername);
        po.setVisitorName(visitorName);
        po.setVisitorPhone(visitorPhone);
        po.setRelationToElder(relationToElder);
        po.setVisitPurpose(visitPurpose);
        po.setStatus(status);
        po.setReviewedBy(reviewedBy);
        po.setReviewComment(reviewComment);
        po.setReviewedAt(reviewedAt);
    }

    private void ensurePending() {
        if (!STATUS_PENDING.equals(status)) {
            throw new IllegalStateException("当前预约状态不允许审核");
        }
    }
}
