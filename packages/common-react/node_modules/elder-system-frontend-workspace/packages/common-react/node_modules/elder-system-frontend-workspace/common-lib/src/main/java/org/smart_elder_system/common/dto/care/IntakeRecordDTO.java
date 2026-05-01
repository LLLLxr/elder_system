package org.smart_elder_system.common.dto.care;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IntakeRecordDTO {

    private Long applicationId;

    private Long elderId;

    private String applicantName;

    private LocalDateTime submittedAt;

    private String admissionStatus;

    private String journeyStatus;

    private String message;
}
