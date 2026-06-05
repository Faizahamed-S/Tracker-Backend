package com.ApplyZap.Tracker.service;

import com.ApplyZap.Tracker.model.ActivityType;
import com.ApplyZap.Tracker.model.Application;
import com.ApplyZap.Tracker.repository.ApplicationActivityLogRepository;
import com.ApplyZap.Tracker.repository.boardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

@Service
public class ApplicationTimestampBackfillService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationTimestampBackfillService.class);

    @Autowired
    private boardRepository applicationRepository;

    @Autowired
    private ApplicationActivityLogRepository activityLogRepository;

    @Transactional
    public int backfillMissingTimestamps() {
        List<Application> applications = applicationRepository.findNeedingTimestampBackfill();
        int updated = 0;
        for (Application app : applications) {
            if (app.getCreatedAt() != null && app.getStatusUpdatedAt() != null) {
                continue;
            }
            LocalDateTime createdAt = resolveCreatedAt(app);
            LocalDateTime statusUpdatedAt = resolveStatusUpdatedAt(app, createdAt);
            if (app.getCreatedAt() == null) {
                app.setCreatedAt(createdAt);
            }
            if (app.getStatusUpdatedAt() == null) {
                app.setStatusUpdatedAt(statusUpdatedAt);
            }
            applicationRepository.save(app);
            updated++;
        }
        log.info("Application timestamp backfill completed: {} row(s) updated", updated);
        return updated;
    }

    private LocalDateTime resolveCreatedAt(Application app) {
        return activityLogRepository
                .findEarliestCreatedAtByApplicationAndType(app.getId(), ActivityType.CREATED)
                .orElseGet(() -> fromDateOfApplication(app.getDateOfApplication()));
    }

    private LocalDateTime resolveStatusUpdatedAt(Application app, LocalDateTime createdAt) {
        return activityLogRepository
                .findLatestCreatedAtByApplicationAndType(app.getId(), ActivityType.STATUS_CHANGE)
                .orElse(createdAt);
    }

    private LocalDateTime fromDateOfApplication(Date dateOfApplication) {
        if (dateOfApplication == null) {
            return LocalDateTime.now();
        }
        return dateOfApplication.toInstant().atZone(ZoneOffset.UTC).toLocalDate().atStartOfDay();
    }
}
