package com.ApplyZap.Tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupAnalyticsDTO {
    private long totalJobs;
    private long jobsAddedToday;
    private GroupContributorDTO topContributorOverall;
    private List<GroupContributorDTO> contributorsOverall;
    private GroupContributorDTO topContributorByDay;
    private List<GroupContributorDTO> contributorsByDay;
    private String date; // the day used for "by day" (YYYY-MM-DD)
}
