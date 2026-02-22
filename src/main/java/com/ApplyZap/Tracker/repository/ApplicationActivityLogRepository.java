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
}
