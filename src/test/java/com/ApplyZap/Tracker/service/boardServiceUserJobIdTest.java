package com.ApplyZap.Tracker.service;

import com.ApplyZap.Tracker.dto.ApplicationCreateDTO;
import com.ApplyZap.Tracker.dto.ApplicationCreateResponseDTO;
import com.ApplyZap.Tracker.model.Application;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class boardServiceUserJobIdTest {

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
        when(repo.save(any(Application.class))).thenAnswer(inv -> {
            Application app = inv.getArgument(0);
            if (app.getId() == null) {
                app.setId(100L);
            }
            return app;
        });
        when(activityLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createApplication_assignsUserJobIdStartingAtOne() {
        when(repo.findMaxUserJobIdByUser(user)).thenReturn(null);

        ApplicationCreateDTO dto = new ApplicationCreateDTO();
        dto.setCompanyName("Acme");
        dto.setRoleName("SWE");
        dto.setStatus("APPLIED");

        ApplicationCreateResponseDTO response = boardService.createApplication(dto);

        assertEquals(1, response.getApplication().getUserJobId());
        ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
        verify(repo).save(captor.capture());
        assertEquals(1, captor.getValue().getUserJobId());
    }

    @Test
    void createApplication_assignsNextUserJobIdAfterMax() {
        when(repo.findMaxUserJobIdByUser(user)).thenReturn(5);

        ApplicationCreateDTO dto = new ApplicationCreateDTO();
        dto.setCompanyName("Beta");
        dto.setRoleName("PM");
        dto.setStatus("WISHLIST");

        ApplicationCreateResponseDTO response = boardService.createApplication(dto);

        assertEquals(6, response.getApplication().getUserJobId());
    }

    @Test
    void updateApplication_doesNotOverwriteUserJobId() {
        Application existing = new Application();
        existing.setId(10L);
        existing.setUser(user);
        existing.setUserJobId(3);
        existing.setCompanyName("Acme");
        existing.setStatus("APPLIED");

        Application incoming = new Application();
        incoming.setCompanyName("Acme Updated");
        incoming.setUserJobId(999);

        when(repo.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        Application result = boardService.updateApplication(existing, incoming);

        assertEquals(3, result.getUserJobId());
        assertEquals("Acme Updated", result.getCompanyName());
    }
}
