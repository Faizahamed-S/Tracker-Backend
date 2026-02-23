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
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private static final int RECENT_ACTIVITY_DAYS = 7;
    private static final int MONTH_VIEW_DAYS = 30;
    private static final int YEAR_VIEW_MONTHS = 12;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

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
        List<DashboardDTO.HourActivityItem> activityByHourToday = buildActivityByHourToday(user);
        List<DashboardDTO.RecentActivityItem> activityByDayLast30 = buildActivityByDayLast30(user);
        List<DashboardDTO.MonthActivityItem> activityByMonthLast12 = buildActivityByMonthLast12(user);

        return new DashboardDTO(summary, recentActivity, activityByHourToday, activityByDayLast30, activityByMonthLast12);
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

    /** Day view: hourly density for today (0-23). Fills missing hours with 0 for consistent charts. */
    private List<DashboardDTO.HourActivityItem> buildActivityByHourToday(User user) {
        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();
        List<Object[]> rows = applicationActivityLogRepository.countByUserAndCreatedAtBetweenGroupByHour(user.getId(), dayStart, dayEnd);
        Map<Integer, Long> hourToCount = new HashMap<>();
        for (Object[] row : rows) {
            Number hourNum = (Number) row[0];
            Number countNum = (Number) row[1];
            int hour = hourNum != null ? hourNum.intValue() : 0;
            long count = countNum != null ? countNum.longValue() : 0;
            hourToCount.put(hour, count);
        }
        List<DashboardDTO.HourActivityItem> result = new ArrayList<>(24);
        for (int hour = 0; hour < 24; hour++) {
            result.add(new DashboardDTO.HourActivityItem(hour, hourToCount.getOrDefault(hour, 0L)));
        }
        return result;
    }

    /** Month view: last 30 days, one count per date. Fills missing days with 0 for consistent charts. */
    private List<DashboardDTO.RecentActivityItem> buildActivityByDayLast30(User user) {
        LocalDateTime since = LocalDateTime.now().minusDays(MONTH_VIEW_DAYS);
        List<Object[]> rows = applicationActivityLogRepository.countByUserAndCreatedAtSinceGroupByDate(user.getId(), since);
        Map<String, Long> dateToCount = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Object dayObj = row[0];
            Number countNum = (Number) row[1];
            String dateStr = formatDateFromRow(dayObj);
            long count = countNum != null ? countNum.longValue() : 0;
            dateToCount.put(dateStr, count);
        }
        List<DashboardDTO.RecentActivityItem> result = new ArrayList<>(MONTH_VIEW_DAYS);
        for (int i = MONTH_VIEW_DAYS - 1; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            String dateStr = d.format(DATE_FORMAT);
            result.add(new DashboardDTO.RecentActivityItem(dateStr, dateToCount.getOrDefault(dateStr, 0L)));
        }
        return result;
    }

    /** Year view: last 12 months, one count per month (YYYY-MM). Fills missing months with 0. */
    private List<DashboardDTO.MonthActivityItem> buildActivityByMonthLast12(User user) {
        LocalDateTime since = LocalDateTime.now().minusMonths(YEAR_VIEW_MONTHS);
        List<Object[]> rows = applicationActivityLogRepository.countByUserAndCreatedAtSinceGroupByMonth(user.getId(), since);
        Map<String, Long> monthToCount = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String monthStr = row[0] != null ? row[0].toString() : null;
            Number countNum = (Number) row[1];
            if (monthStr != null) {
                long count = countNum != null ? countNum.longValue() : 0;
                monthToCount.put(monthStr, count);
            }
        }
        List<DashboardDTO.MonthActivityItem> result = new ArrayList<>(YEAR_VIEW_MONTHS);
        for (int i = YEAR_VIEW_MONTHS - 1; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            String monthStr = ym.format(MONTH_FORMAT);
            result.add(new DashboardDTO.MonthActivityItem(monthStr, monthToCount.getOrDefault(monthStr, 0L)));
        }
        return result;
    }

    private String formatDateFromRow(Object dayObj) {
        if (dayObj instanceof LocalDate) {
            return ((LocalDate) dayObj).format(DATE_FORMAT);
        }
        if (dayObj instanceof java.sql.Date) {
            return ((java.sql.Date) dayObj).toLocalDate().format(DATE_FORMAT);
        }
        return dayObj != null ? dayObj.toString() : "";
    }
}
