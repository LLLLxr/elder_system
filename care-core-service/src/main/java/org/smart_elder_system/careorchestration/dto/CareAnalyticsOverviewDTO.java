package org.smart_elder_system.careorchestration.dto;

import lombok.Data;

import java.util.List;

@Data
public class CareAnalyticsOverviewDTO {

    private Integer applicationsTotal;
    private Integer agreementsActive;
    private Integer plansInProgress;
    private Integer averageSatisfaction;
    private List<StagePoint> stageDistribution;

    @Data
    public static class StagePoint {
        private String name;
        private Integer value;
    }
}
