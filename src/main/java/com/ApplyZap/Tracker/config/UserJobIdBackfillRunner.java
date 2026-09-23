package com.ApplyZap.Tracker.config;

import com.ApplyZap.Tracker.service.UserJobIdBackfillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "applyzap.backfill.user-job-ids", havingValue = "true")
public class UserJobIdBackfillRunner implements ApplicationRunner {

    @Autowired
    private UserJobIdBackfillService backfillService;

    @Override
    public void run(ApplicationArguments args) {
        backfillService.backfillMissingUserJobIds();
    }
}
