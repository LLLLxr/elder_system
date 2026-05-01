package org.smart_elder_system.admission.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EligibilityAssessment {

    private Boolean eligible;

    private String assessmentConclusion;

    private String assessor;

    private LocalDateTime assessedAt;
}
