package org.smart_elder_system.user.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserAnalyticsOverviewDTO {

    private Integer totalUsers;
    private Integer activeUsers;
    private Integer disabledUsers;
    private Integer newUsers30Days;
    private List<TrendPoint> growthTrend;

    @Data
    public static class TrendPoint {
        private String label;
        private Integer value;
    }
}
