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
    /** Week view: last 7 days, one count per day (unchanged for compatibility). */
    private List<RecentActivityItem> recent_activity;
    /** Day view: hourly density for today (0-23). */
    private List<HourActivityItem> activity_by_hour_today;
    /** Month view: last 30 days, one count per date. */
    private List<RecentActivityItem> activity_by_day_last_30;
    /** Year view: last 12 months, one count per month (YYYY-MM). */
    private List<MonthActivityItem> activity_by_month_last_12;

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourActivityItem {
        private int hour;
        private long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthActivityItem {
        private String month;
        private long count;
    }
}
