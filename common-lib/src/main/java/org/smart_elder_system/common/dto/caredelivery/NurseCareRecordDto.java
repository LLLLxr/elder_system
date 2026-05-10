package org.smart_elder_system.common.dto.caredelivery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NurseCareRecordDto {

    private Long recordId;
    private Long elderId;
    private String elderName;
    private Long nurseId;
    private String nurseName;
    private Long servicePlanId;
    private LocalDate recordDate;
    private Map<String, Object> recordFormData;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
