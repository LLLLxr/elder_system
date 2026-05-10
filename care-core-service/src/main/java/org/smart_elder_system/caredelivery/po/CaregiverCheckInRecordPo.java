package org.smart_elder_system.caredelivery.po;

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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "caregiver_check_in_record")
public class CaregiverCheckInRecordPo extends JpaUserAuditablePo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "elder_id", nullable = false)
    private Long elderId;

    @Column(name = "elder_name", length = 100)
    private String elderName;

    @Column(name = "caregiver_id", nullable = false)
    private Long caregiverId;

    @Column(name = "caregiver_name", length = 100)
    private String caregiverName;

    @Column(name = "service_plan_id", nullable = false)
    private Long servicePlanId;

    @Column(name = "task_date", nullable = false)
    private LocalDate taskDate;

    @Column(name = "task_items_json", nullable = false, length = 5000)
    private String taskItemsJson;

    @Column(name = "completion_status", nullable = false, length = 32)
    private String completionStatus;

    @Column(name = "completion_time")
    private LocalDateTime completionTime;
}
