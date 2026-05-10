package org.smart_elder_system.common.dto.health;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorRoundRecordDto {

    private Long roundRecordId;
    private Long elderId;
    private String elderName;
    private Long doctorId;
    private String doctorName;
    private String content;
    private Boolean riskFlag;
    private LocalDateTime roundTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
