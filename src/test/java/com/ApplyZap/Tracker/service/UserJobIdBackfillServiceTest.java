package com.ApplyZap.Tracker.service;

import com.ApplyZap.Tracker.model.Application;
import com.ApplyZap.Tracker.model.User;
import com.ApplyZap.Tracker.repository.boardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserJobIdBackfillServiceTest {

    @Mock
    private boardRepository applicationRepository;

    @InjectMocks
    private UserJobIdBackfillService backfillService;

    @Test
    void backfill_assignsSequentialIdsInCreatedOrder() {
        User user = new User();
        user.setId(1L);

        Application older = new Application();
        older.setId(10L);
        older.setUser(user);
        older.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));

        Application newer = new Application();
        newer.setId(11L);
        newer.setUser(user);
        newer.setCreatedAt(LocalDateTime.of(2026, 2, 1, 0, 0));

        when(applicationRepository.findUsersNeedingUserJobIdBackfill()).thenReturn(List.of(user));
        when(applicationRepository.findMaxUserJobIdByUser(user)).thenReturn(null);
        when(applicationRepository.findByUserAndUserJobIdIsNullOrderByCreatedAtAscIdAsc(user))
                .thenReturn(List.of(older, newer));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        int updated = backfillService.backfillMissingUserJobIds();

        assertEquals(2, updated);
        assertEquals(1, older.getUserJobId());
        assertEquals(2, newer.getUserJobId());
        verify(applicationRepository).save(older);
        verify(applicationRepository).save(newer);
    }

    @Test
    void backfill_startsAfterExistingMax() {
        User user = new User();
        user.setId(2L);

        Application missing = new Application();
        missing.setId(20L);
        missing.setUser(user);
        missing.setCreatedAt(LocalDateTime.of(2026, 3, 1, 0, 0));

        when(applicationRepository.findUsersNeedingUserJobIdBackfill()).thenReturn(List.of(user));
        when(applicationRepository.findMaxUserJobIdByUser(user)).thenReturn(4);
        when(applicationRepository.findByUserAndUserJobIdIsNullOrderByCreatedAtAscIdAsc(user))
                .thenReturn(List.of(missing));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        int updated = backfillService.backfillMissingUserJobIds();

        assertEquals(1, updated);
        assertEquals(5, missing.getUserJobId());
    }
}
