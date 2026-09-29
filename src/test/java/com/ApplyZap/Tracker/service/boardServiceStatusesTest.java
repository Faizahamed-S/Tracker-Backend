package com.ApplyZap.Tracker.service;

import com.ApplyZap.Tracker.model.User;
import com.ApplyZap.Tracker.repository.ApplicationActivityLogRepository;
import com.ApplyZap.Tracker.repository.ReferralContactRepository;
import com.ApplyZap.Tracker.repository.boardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class boardServiceStatusesTest {

    @Mock
    private boardRepository repo;
    @Mock
    private userService userService;
    @Mock
    private ApplicationActivityLogRepository activityLogRepository;
    @Mock
    private GroupJobService groupJobService;
    @Mock
    private ReferralContactRepository referralContactRepository;

    @InjectMocks
    private boardService boardService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        when(userService.getCurrentUser()).thenReturn(user);
    }

    private void columns(String... titles) {
        user.setTrackerConfig(Map.of("columns",
                java.util.Arrays.stream(titles).map(t -> Map.of("title", t)).toList()));
    }

    @Test
    void columnsInBoardOrder_thenExtrasSorted() {
        columns("Wishlist", "Phone Screen", "Applied");
        when(repo.findDistinctStatusesByUser(user)).thenReturn(List.of("Zeta", "applied", "Archived"));

        assertEquals(List.of("WISHLIST", "PHONE_SCREEN", "APPLIED", "ARCHIVED", "ZETA"),
                boardService.getUniqueStatuses());
    }

    @Test
    void inUseStatusMatchingColumn_isNotDuplicated() {
        columns("Phone Screen");
        when(repo.findDistinctStatusesByUser(user)).thenReturn(List.of("PHONE_SCREEN", "phone screen"));

        assertEquals(List.of("PHONE_SCREEN"), boardService.getUniqueStatuses());
    }

    @Test
    void noColumns_returnsOnlyInUseSorted() {
        user.setTrackerConfig(null);
        when(repo.findDistinctStatusesByUser(user)).thenReturn(List.of("REJECTED", "applied", "APPLIED"));

        assertEquals(List.of("APPLIED", "REJECTED"), boardService.getUniqueStatuses());
    }

    @Test
    void unusedColumn_stillAppears() {
        columns("Wishlist", "Ghosted");
        when(repo.findDistinctStatusesByUser(user)).thenReturn(List.of("WISHLIST"));

        assertEquals(List.of("WISHLIST", "GHOSTED"), boardService.getUniqueStatuses());
    }
}
