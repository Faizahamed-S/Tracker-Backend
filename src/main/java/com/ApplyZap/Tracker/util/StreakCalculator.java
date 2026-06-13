package com.ApplyZap.Tracker.util;

import com.ApplyZap.Tracker.model.ActivityType;
import com.ApplyZap.Tracker.model.ApplicationActivityLog;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Computes activity streaks from application_activity_log.
 * A day is active when the user creates an application or changes status away from
 * that application's initial (create-time) status. Moving back to initial status does not count.
 */
public final class StreakCalculator {

    private StreakCalculator() {
    }

    public record StreakResult(long currentStreak, long longestStreak) {
    }

    public static StreakResult compute(List<ApplicationActivityLog> logs) {
        return compute(logs, LocalDate.now(ZoneOffset.UTC));
    }

    public static StreakResult compute(List<ApplicationActivityLog> logs, LocalDate todayUtc) {
        if (logs == null || logs.isEmpty()) {
            return new StreakResult(0, 0);
        }

        Map<Long, String> initialStatusByApp = new HashMap<>();
        for (ApplicationActivityLog log : logs) {
            if (log.getActivityType() == ActivityType.CREATED && log.getApplication() != null) {
                Long appId = log.getApplication().getId();
                String normalized = StatusNormalizer.normalize(log.getNewStatus());
                if (normalized != null) {
                    initialStatusByApp.putIfAbsent(appId, normalized);
                }
            }
        }

        Set<LocalDate> activeDays = new HashSet<>();
        for (ApplicationActivityLog log : logs) {
            if (log.getApplication() == null || log.getCreatedAt() == null) {
                continue;
            }
            Long appId = log.getApplication().getId();
            LocalDate day = log.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate();

            if (log.getActivityType() == ActivityType.CREATED) {
                activeDays.add(day);
                continue;
            }

            if (log.getActivityType() == ActivityType.STATUS_CHANGE) {
                if (!initialStatusByApp.containsKey(appId)) {
                    String seed = StatusNormalizer.normalize(log.getPreviousStatus());
                    if (seed != null) {
                        initialStatusByApp.put(appId, seed);
                    }
                }
                String initial = initialStatusByApp.get(appId);
                String newStatus = StatusNormalizer.normalize(log.getNewStatus());
                if (initial == null || newStatus == null) {
                    activeDays.add(day);
                    continue;
                }
                if (!newStatus.equals(initial)) {
                    activeDays.add(day);
                }
            }
        }

        long longest = computeLongestStreak(activeDays);
        long current = computeCurrentStreak(activeDays, todayUtc);
        return new StreakResult(current, longest);
    }

    private static long computeLongestStreak(Set<LocalDate> activeDays) {
        if (activeDays.isEmpty()) {
            return 0;
        }
        SortedSet<LocalDate> sorted = new TreeSet<>(activeDays);
        long best = 0;
        long run = 0;
        LocalDate prev = null;
        for (LocalDate d : sorted) {
            if (prev != null && d.equals(prev.plusDays(1))) {
                run++;
            } else {
                run = 1;
            }
            best = Math.max(best, run);
            prev = d;
        }
        return best;
    }

    private static long computeCurrentStreak(Set<LocalDate> activeDays, LocalDate today) {
        if (activeDays.isEmpty()) {
            return 0;
        }
        LocalDate start;
        if (activeDays.contains(today)) {
            start = today;
        } else if (activeDays.contains(today.minusDays(1))) {
            start = today.minusDays(1);
        } else {
            return 0;
        }
        long streak = 0;
        LocalDate cursor = start;
        while (activeDays.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }
}
