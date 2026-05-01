package org.smart_elder_system.careorchestration.dto;

import lombok.Data;

import java.util.List;

@Data
public class CareAnalyticsTrendsDTO {

    private List<TrendPoint> applicationTrend;
    private List<TrendPoint> agreementTrend;
    private List<TrendPoint> reviewTrend;

    @Data
    public static class TrendPoint {
        private String label;
        private Integer value;
    }
}
