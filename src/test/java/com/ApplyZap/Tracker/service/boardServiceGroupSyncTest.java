package com.ApplyZap.Tracker.service;

import com.ApplyZap.Tracker.dto.GroupAddResultDTO;
import com.ApplyZap.Tracker.dto.GroupJobCreateDTO;
import com.ApplyZap.Tracker.model.Application;
import com.ApplyZap.Tracker.model.GroupJob;
import com.ApplyZap.Tracker.model.User;
import com.ApplyZap.Tracker.repository.ApplicationActivityLogRepository;
import com.ApplyZap.Tracker.repository.ReferralContactRepository;
import com.ApplyZap.Tracker.repository.boardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class boardServiceGroupSyncTest {

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
    private Application existing;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        when(userService.getCurrentUser()).thenReturn(user);

        existing = new Application();
        existing.setId(10L);
        existing.setUser(user);
        existing.setCompanyName("Acme");
        existing.setRoleName("Engineer");
        existing.setJobLink("https://jobs.acme.com/1");
        existing.setStatus("Applied");
    }

    @Test
    void update_withGroupIds_callsCreateJobAndReturnsGroupResults() {
        Application incoming = new Application();
        incoming.setGroupIds(List.of(1L));

        when(repo.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        GroupJob job = new GroupJob();
        job.setId(99L);
        when(groupJobService.createJob(eq(1L), any(GroupJobCreateDTO.class))).thenReturn(job);

        Application result = boardService.updateApplication(existing, incoming);

        verify(groupJobService).createJob(eq(1L), any(GroupJobCreateDTO.class));
        assertEquals(1, result.getGroupResults().size());
        assertTrue(result.getGroupResults().get(0).isSuccess());
        assertEquals(99L, result.getGroupResults().get(0).getJobId());
    }

    @Test
    void update_withoutGroupIds_doesNotCallCreateJob() {
        Application incoming = new Application();
        incoming.setCompanyName("NewCo");

        when(repo.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        Application result = boardService.updateApplication(existing, incoming);

        verify(groupJobService, never()).createJob(any(), any());
        assertNull(result.getGroupResults());
    }

    @Test
    void update_withGroupIdsAndBlankJobLink_returnsFailureInGroupResults() {
        existing.setJobLink("  ");
        Application incoming = new Application();
        incoming.setGroupIds(List.of(2L));

        when(repo.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        Application result = boardService.updateApplication(existing, incoming);

        verify(groupJobService, never()).createJob(any(), any());
        assertEquals(1, result.getGroupResults().size());
        assertEquals(false, result.getGroupResults().get(0).isSuccess());
        assertTrue(result.getGroupResults().get(0).getError().contains("Job link is required"));
    }

    @Test
    void update_onlyGroupIds_usesExistingJobMetadata() {
        Application incoming = new Application();
        incoming.setGroupIds(List.of(3L));

        when(repo.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        GroupJob job = new GroupJob();
        job.setId(50L);
        when(groupJobService.createJob(eq(3L), any(GroupJobCreateDTO.class))).thenReturn(job);

        boardService.updateApplication(existing, incoming);

        ArgumentCaptor<GroupJobCreateDTO> captor = ArgumentCaptor.forClass(GroupJobCreateDTO.class);
        verify(groupJobService).createJob(eq(3L), captor.capture());
        GroupJobCreateDTO dto = captor.getValue();
        assertEquals("https://jobs.acme.com/1", dto.getJobLink());
        assertEquals("Acme", dto.getCompanyName());
        assertEquals("Engineer", dto.getRoleName());
    }

    @Test
    void update_withEmptyGroupIds_returnsEmptyGroupResults() {
        Application incoming = new Application();
        incoming.setGroupIds(List.of());

        when(repo.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        Application result = boardService.updateApplication(existing, incoming);

        verify(groupJobService, never()).createJob(any(), any());
        assertTrue(result.getGroupResults().isEmpty());
    }
}
