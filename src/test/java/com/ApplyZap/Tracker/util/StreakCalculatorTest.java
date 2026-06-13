package com.ApplyZap.Tracker.util;

import com.ApplyZap.Tracker.model.ActivityType;
import com.ApplyZap.Tracker.model.Application;
import com.ApplyZap.Tracker.model.ApplicationActivityLog;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StreakCalculatorTest {

    private static final Long APP_ID = 1L;

    @Test
    void emptyLogs_returnsZero() {
        StreakCalculator.StreakResult result = StreakCalculator.compute(List.of(), LocalDate.of(2026, 6, 5));
        assertEquals(0, result.currentStreak());
        assertEquals(0, result.longestStreak());
    }

    @Test
    void threeConsecutiveCreateDays_currentAndLongestAreThree() {
        LocalDate today = LocalDate.of(2026, 6, 5);
        List<ApplicationActivityLog> logs = List.of(
                created(APP_ID, "Applied", dayAt(today.minusDays(2), 10)),
                created(APP_ID, "Applied", dayAt(today.minusDays(1), 10)),
                created(APP_ID, "Applied", dayAt(today, 10)));

        StreakCalculator.StreakResult result = StreakCalculator.compute(logs, today);
        assertEquals(3, result.currentStreak());
        assertEquals(3, result.longestStreak());
    }

    @Test
    void revertToInitialSameDay_countsOneActiveDay() {
        LocalDate today = LocalDate.of(2026, 6, 5);
        List<ApplicationActivityLog> logs = List.of(
                created(APP_ID, "Applied", dayAt(today, 9)),
                statusChange(APP_ID, "Applied", "Interview", dayAt(today, 10)),
                statusChange(APP_ID, "Interview", "Applied", dayAt(today, 11)));

        StreakCalculator.StreakResult result = StreakCalculator.compute(logs, today);
        assertEquals(1, result.currentStreak());
        assertEquals(1, result.longestStreak());
    }

    @Test
    void appliedToInterviewOnDifferentDays_countsTwoActiveDays() {
        LocalDate today = LocalDate.of(2026, 6, 5);
        List<ApplicationActivityLog> logs = List.of(
                created(APP_ID, "Applied", dayAt(today.minusDays(1), 10)),
                statusChange(APP_ID, "Applied", "Interview", dayAt(today, 10)));

        StreakCalculator.StreakResult result = StreakCalculator.compute(logs, today);
        assertEquals(2, result.currentStreak());
        assertEquals(2, result.longestStreak());
    }

    @Test
    void revertToInitialOnLaterDay_doesNotAddActiveDay() {
        LocalDate today = LocalDate.of(2026, 6, 5);
        List<ApplicationActivityLog> logs = List.of(
                created(APP_ID, "Applied", dayAt(today.minusDays(2), 10)),
                statusChange(APP_ID, "Applied", "Interview", dayAt(today.minusDays(1), 10)),
                statusChange(APP_ID, "Interview", "Applied", dayAt(today, 10)));

        StreakCalculator.StreakResult result = StreakCalculator.compute(logs, today);
        assertEquals(2, result.currentStreak());
        assertEquals(2, result.longestStreak());
    }

    @Test
    void interviewAgainAfterRejected_countsThirdDay() {
        LocalDate today = LocalDate.of(2026, 6, 5);
        List<ApplicationActivityLog> logs = List.of(
                created(APP_ID, "Applied", dayAt(today.minusDays(3), 10)),
                statusChange(APP_ID, "Applied", "Interview", dayAt(today.minusDays(2), 10)),
                statusChange(APP_ID, "Interview", "Rejected", dayAt(today.minusDays(1), 10)),
                statusChange(APP_ID, "Rejected", "Interview", dayAt(today, 10)));

        StreakCalculator.StreakResult result = StreakCalculator.compute(logs, today);
        assertEquals(4, result.currentStreak());
        assertEquals(4, result.longestStreak());
    }

    @Test
    void gapBreaksCurrent_longestPreservesHistoricalBest() {
        LocalDate today = LocalDate.of(2026, 6, 10);
        List<ApplicationActivityLog> logs = List.of(
                created(APP_ID, "Applied", dayAt(LocalDate.of(2026, 6, 1), 10)),
                created(APP_ID, "Applied", dayAt(LocalDate.of(2026, 6, 2), 10)),
                created(APP_ID, "Applied", dayAt(LocalDate.of(2026, 6, 3), 10)),
                created(APP_ID, "Applied", dayAt(today, 10)));

        StreakCalculator.StreakResult result = StreakCalculator.compute(logs, today);
        assertEquals(1, result.currentStreak());
        assertEquals(3, result.longestStreak());
    }

    @Test
    void grace_yesterdayActiveTodayNot_currentStillCountsFromYesterday() {
        LocalDate today = LocalDate.of(2026, 6, 5);
        List<ApplicationActivityLog> logs = List.of(
                created(APP_ID, "Applied", dayAt(today.minusDays(2), 10)),
                created(APP_ID, "Applied", dayAt(today.minusDays(1), 10)));

        StreakCalculator.StreakResult result = StreakCalculator.compute(logs, today);
        assertEquals(2, result.currentStreak());
        assertEquals(2, result.longestStreak());
    }

    @Test
    void legacyApp_statusChangeOnly_seedsInitialFromPreviousStatus() {
        LocalDate today = LocalDate.of(2026, 6, 5);
        List<ApplicationActivityLog> logs = List.of(
                statusChange(APP_ID, "Applied", "Interview", dayAt(today, 10)),
                statusChange(APP_ID, "Interview", "Applied", dayAt(today, 11)));

        StreakCalculator.StreakResult result = StreakCalculator.compute(logs, today);
        assertEquals(1, result.currentStreak());
        assertEquals(1, result.longestStreak());
    }

    private static ApplicationActivityLog created(Long appId, String status, LocalDateTime at) {
        ApplicationActivityLog log = new ApplicationActivityLog();
        log.setApplication(app(appId));
        log.setActivityType(ActivityType.CREATED);
        log.setNewStatus(status);
        log.setCreatedAt(at);
        return log;
    }

    private static ApplicationActivityLog statusChange(Long appId, String from, String to, LocalDateTime at) {
        ApplicationActivityLog log = new ApplicationActivityLog();
        log.setApplication(app(appId));
        log.setActivityType(ActivityType.STATUS_CHANGE);
        log.setPreviousStatus(from);
        log.setNewStatus(to);
        log.setCreatedAt(at);
        return log;
    }

    private static Application app(Long id) {
        Application application = new Application();
        application.setId(id);
        return application;
    }

    private static LocalDateTime dayAt(LocalDate day, int hour) {
        return day.atTime(hour, 0).atZone(ZoneOffset.UTC).toLocalDateTime();
    }
}
