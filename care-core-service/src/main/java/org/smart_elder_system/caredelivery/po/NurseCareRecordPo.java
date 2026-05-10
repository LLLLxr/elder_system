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

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "nurse_care_record")
public class NurseCareRecordPo extends JpaUserAuditablePo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "elder_id", nullable = false)
    private Long elderId;

    @Column(name = "elder_name", length = 100)
    private String elderName;

    @Column(name = "nurse_id", nullable = false)
    private Long nurseId;

    @Column(name = "nurse_name", length = 100)
    private String nurseName;

    @Column(name = "service_plan_id")
    private Long servicePlanId;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "record_form_data", nullable = false, length = 5000)
    private String recordFormData;
}
