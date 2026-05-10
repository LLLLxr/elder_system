package org.smart_elder_system.quality.po;

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
@Table(name = "quality_caregiver_qualification_application")
public class CaregiverQualificationApplicationPo extends JpaUserAuditablePo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "caregiver_user_id", nullable = false)
    private Long caregiverUserId;

    @Column(name = "caregiver_username", length = 100, nullable = false)
    private String caregiverUsername;

    @Column(name = "real_name", length = 100, nullable = false)
    private String realName;

    @Column(name = "phone", length = 32, nullable = false)
    private String phone;

    @Column(name = "id_card_no", length = 32, nullable = false)
    private String idCardNo;

    @Column(name = "certificate_no", length = 64, nullable = false)
    private String certificateNo;

    @Column(name = "certificate_type", length = 64, nullable = false)
    private String certificateType;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "skill_summary", length = 1000, nullable = false)
    private String skillSummary;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;

    @Column(name = "review_comment", length = 500)
    private String reviewComment;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "active_flag")
    private Integer activeFlag;
}
