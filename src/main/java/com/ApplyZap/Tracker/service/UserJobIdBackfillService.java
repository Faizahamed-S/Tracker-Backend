package com.ApplyZap.Tracker.service;

import com.ApplyZap.Tracker.model.Application;
import com.ApplyZap.Tracker.model.User;
import com.ApplyZap.Tracker.repository.boardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserJobIdBackfillService {

    private static final Logger log = LoggerFactory.getLogger(UserJobIdBackfillService.class);

    @Autowired
    private boardRepository applicationRepository;

    /**
     * Assigns sequential userJobId values to applications that are missing them.
     * Per user: order by createdAt asc, id asc; start after current max (or 1).
     */
    @Transactional
    public int backfillMissingUserJobIds() {
        List<User> users = applicationRepository.findUsersNeedingUserJobIdBackfill();
        int updated = 0;
        for (User user : users) {
            Integer max = applicationRepository.findMaxUserJobIdByUser(user);
            int next = (max != null ? max : 0) + 1;
            List<Application> missing = applicationRepository
                    .findByUserAndUserJobIdIsNullOrderByCreatedAtAscIdAsc(user);
            for (Application app : missing) {
                app.setUserJobId(next++);
                applicationRepository.save(app);
                updated++;
            }
        }
        log.info("Application userJobId backfill completed: {} row(s) updated across {} user(s)",
                updated, users.size());
        return updated;
    }
}
