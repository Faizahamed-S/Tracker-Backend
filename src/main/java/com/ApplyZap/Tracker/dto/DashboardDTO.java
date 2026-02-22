package com.ApplyZap.Tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {

    private Summary summary;
    private List<RecentActivityItem> recent_activity;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private long totalApplications;
        private long interviews;
        private long offers;
        private Map<String, Long> statusCounts;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActivityItem {
        private String date;
        private long count;
    }
}
