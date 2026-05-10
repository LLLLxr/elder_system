package org.smart_elder_system.admission.po;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.smart_elder_system.common.po.JpaUserAuditablePo;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "admission_family_visit_reservation")
public class FamilyVisitReservationPo extends JpaUserAuditablePo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slot_id", nullable = false)
    private Long slotId;

    @Column(name = "elder_id", nullable = false)
    private Long elderId;

    @Column(name = "family_user_id", nullable = false)
    private Long familyUserId;

    @Column(name = "family_username", length = 100, nullable = false)
    private String familyUsername;

    @Column(name = "visitor_name", length = 100, nullable = false)
    private String visitorName;

    @Column(name = "visitor_phone", length = 32, nullable = false)
    private String visitorPhone;

    @Column(name = "relation_to_elder", length = 64, nullable = false)
    private String relationToElder;

    @Column(name = "visit_purpose", length = 500, nullable = false)
    private String visitPurpose;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;

    @Column(name = "review_comment", length = 500)
    private String reviewComment;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
}
