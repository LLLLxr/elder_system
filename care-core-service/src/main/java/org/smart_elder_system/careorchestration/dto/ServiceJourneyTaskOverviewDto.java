package org.smart_elder_system.careorchestration.dto;

import lombok.Data;

import java.util.List;

@Data
public class ServiceJourneyTaskOverviewDto {

    private int pendingCount;
    private int overdueCount;
    private int completedCount;
    private int cancelledCount;
    private List<CareAnalyticsOverviewDto.StagePoint> taskTypeDistribution;
    private List<CareAnalyticsOverviewDto.StagePoint> statusDistribution;
}
