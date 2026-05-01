package org.smart_elder_system.careorchestration.dto;

import lombok.Data;

import java.util.List;

@Data
public class ServiceJourneyTaskOverviewDTO {

    private int pendingCount;
    private int overdueCount;
    private int completedCount;
    private int cancelledCount;
    private List<CareAnalyticsOverviewDTO.StagePoint> taskTypeDistribution;
    private List<CareAnalyticsOverviewDTO.StagePoint> statusDistribution;
}
