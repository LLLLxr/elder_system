package org.smart_elder_system.user.po;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.smart_elder_system.common.po.JpaUserAuditablePo;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user_elder_binding_request")
public class UserElderBindingRequestPo extends JpaUserAuditablePo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "applicant_user_id", nullable = false)
    private Long applicantUserId;

    @Column(name = "elder_id")
    private Long elderId;

    @Column(name = "elder_name", nullable = false, length = 100)
    private String elderName;

    @Column(name = "elder_id_card", nullable = false, length = 32)
    private String elderIdCard;

    @Column(name = "elder_phone", length = 32)
    private String elderPhone;

    @Column(name = "relation_to_elder", nullable = false, length = 64)
    private String relationToElder;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;

    @Column(name = "review_comment", length = 500)
    private String reviewComment;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
}
