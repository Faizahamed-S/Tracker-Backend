package com.ApplyZap.Tracker.config;

import com.ApplyZap.Tracker.service.ApplicationTimestampBackfillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "applyzap.backfill.application-timestamps", havingValue = "true")
public class ApplicationTimestampBackfillRunner implements ApplicationRunner {

    @Autowired
    private ApplicationTimestampBackfillService backfillService;

    @Override
    public void run(ApplicationArguments args) {
        backfillService.backfillMissingTimestamps();
    }
}
