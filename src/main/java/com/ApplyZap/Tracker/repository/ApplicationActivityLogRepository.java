package com.ApplyZap.Tracker.repository;

import com.ApplyZap.Tracker.model.ApplicationActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ApplicationActivityLogRepository extends JpaRepository<ApplicationActivityLog, Long> {

    /**
     * Count activity log entries by date for a user since a given time (e.g. last 7 days).
     * Returns list of [date, count] for dashboard recent activity.
     * Uses native query for PostgreSQL date truncation.
     */
    @Query(value = "SELECT CAST(l.created_at AS date) AS day, COUNT(*) FROM application_activity_log l " +
            "WHERE l.user_id = :userId AND l.created_at >= :since " +
            "GROUP BY CAST(l.created_at AS date) ORDER BY day", nativeQuery = true)
    List<Object[]> countByUserAndCreatedAtSinceGroupByDate(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    /**
     * Count activity log entries by hour (0-23) for a user on a single day.
     * For day view: hourly application density. PostgreSQL.
     */
    @Query(value = "SELECT EXTRACT(HOUR FROM l.created_at)::int AS hour, COUNT(*) FROM application_activity_log l " +
            "WHERE l.user_id = :userId AND l.created_at >= :dayStart AND l.created_at < :dayEnd " +
            "GROUP BY EXTRACT(HOUR FROM l.created_at) ORDER BY hour", nativeQuery = true)
    List<Object[]> countByUserAndCreatedAtBetweenGroupByHour(@Param("userId") Long userId,
            @Param("dayStart") LocalDateTime dayStart, @Param("dayEnd") LocalDateTime dayEnd);

    /**
     * Count activity log entries by month (YYYY-MM) for a user since a given time (e.g. last 12 months).
     * For year view. PostgreSQL.
     */
    @Query(value = "SELECT to_char(date_trunc('month', l.created_at), 'YYYY-MM') AS month, COUNT(*) FROM application_activity_log l " +
            "WHERE l.user_id = :userId AND l.created_at >= :since " +
            "GROUP BY date_trunc('month', l.created_at) ORDER BY month", nativeQuery = true)
    List<Object[]> countByUserAndCreatedAtSinceGroupByMonth(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    /**
     * Delete all activity log entries for an application (e.g. before deleting the application).
     */
    void deleteByApplication_Id(Long applicationId);
}
