package com.ApplyZap.Tracker.service;

import com.ApplyZap.Tracker.dto.DashboardDTO;
import com.ApplyZap.Tracker.model.User;
import com.ApplyZap.Tracker.repository.ApplicationActivityLogRepository;
import com.ApplyZap.Tracker.repository.boardRepository;
import com.ApplyZap.Tracker.util.StatusNormalizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private static final int RECENT_ACTIVITY_DAYS = 7;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    /** Status values that count as "interviews" (case-insensitive match). */
    private static final Set<String> INTERVIEW_STATUSES = Set.of("Interviewing", "Interview", "Interviews");

    /** Status value that counts as "offers". */
    private static final String OFFER_STATUS = "Offer";

    @Autowired
    private userService userService;

    @Autowired
    private boardRepository boardRepository;

    @Autowired
    private ApplicationActivityLogRepository applicationActivityLogRepository;

    /**
     * Build dashboard for the current user: summary from Application table (hybrid with existing data)
     * and recent activity from application_activity_log (last 7 days).
     */
    public DashboardDTO getDashboard() {
        User user = userService.getCurrentUser();

        DashboardDTO.Summary summary = buildSummary(user);
        List<DashboardDTO.RecentActivityItem> recentActivity = buildRecentActivity(user);

        return new DashboardDTO(summary, recentActivity);
    }

    private DashboardDTO.Summary buildSummary(User user) {
        List<Object[]> statusCountsRaw = boardRepository.countByUserGroupByStatus(user);

        long totalApplications = 0;
        long interviews = 0;
        long offers = 0;
        Map<String, Long> statusCounts = new LinkedHashMap<>();

        for (Object[] row : statusCountsRaw) {
            String status = row[0] != null ? row[0].toString() : null;
            Number count = (Number) row[1];
            long c = count != null ? count.longValue() : 0;
            totalApplications += c;
            if (status != null) {
                String canonical = StatusNormalizer.normalize(status);
                if (canonical != null) {
                    statusCounts.merge(canonical, c, Long::sum);
                }
                if (INTERVIEW_STATUSES.stream().anyMatch(s -> s.equalsIgnoreCase(status))) {
                    interviews += c;
                }
                if (OFFER_STATUS.equalsIgnoreCase(status)) {
                    offers += c;
                }
            }
        }

        return new DashboardDTO.Summary(totalApplications, interviews, offers, statusCounts);
    }

    private List<DashboardDTO.RecentActivityItem> buildRecentActivity(User user) {
        LocalDateTime since = LocalDateTime.now().minusDays(RECENT_ACTIVITY_DAYS);
        List<Object[]> rows = applicationActivityLogRepository.countByUserAndCreatedAtSinceGroupByDate(user.getId(), since);

        return rows.stream()
                .map(row -> {
                    Object dayObj = row[0];
                    Number countNum = (Number) row[1];
                    String dateStr;
                    if (dayObj instanceof LocalDate) {
                        dateStr = ((LocalDate) dayObj).format(DATE_FORMAT);
                    } else if (dayObj instanceof java.sql.Date) {
                        dateStr = ((java.sql.Date) dayObj).toLocalDate().format(DATE_FORMAT);
                    } else {
                        dateStr = dayObj != null ? dayObj.toString() : "";
                    }
                    long count = countNum != null ? countNum.longValue() : 0;
                    return new DashboardDTO.RecentActivityItem(dateStr, count);
                })
                .collect(Collectors.toList());
    }
}
