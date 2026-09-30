package com.ApplyZap.Tracker.service;

import com.ApplyZap.Tracker.dto.ApplicationUpdateDTO;
import com.ApplyZap.Tracker.model.ActivityType;
import com.ApplyZap.Tracker.model.Application;
import com.ApplyZap.Tracker.model.ApplicationActivityLog;
import com.ApplyZap.Tracker.model.ReferralContact;
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

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class boardServicePartialUpdateTest {

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
    private ReferralContact contact;
    private Application existing;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        when(userService.getCurrentUser()).thenReturn(user);
        when(repo.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));
        when(activityLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        contact = new ReferralContact();
        contact.setId(7L);
        contact.setName("Jane");

        existing = new Application();
        existing.setId(10L);
        existing.setUser(user);
        existing.setCompanyName("Acme");
        existing.setRoleName("SWE");
        existing.setStatus("APPLIED");
        existing.setReferral(true);
        existing.setReferralContact(contact);
        existing.setTailored(false);
    }

    @Test
    void tailoredOnly_keepsReferralAndContact() {
        ApplicationUpdateDTO dto = new ApplicationUpdateDTO();
        dto.setTailored(true);

        Application result = boardService.updateApplication(existing, dto);

        assertTrue(result.isTailored());
        assertTrue(result.isReferral());
        assertSame(contact, result.getReferralContact());
        assertEquals(7L, result.getReferralContactId());
    }

    @Test
    void statusOnly_keepsReferralAndLogsStatusChange() {
        ApplicationUpdateDTO dto = new ApplicationUpdateDTO();
        dto.setStatus("Interviewing");

        Application result = boardService.updateApplication(existing, dto);

        assertEquals("INTERVIEWING", result.getStatus());
        assertTrue(result.isReferral());
        assertSame(contact, result.getReferralContact());

        ArgumentCaptor<ApplicationActivityLog> captor = ArgumentCaptor.forClass(ApplicationActivityLog.class);
        verify(activityLogRepository).save(captor.capture());
        assertEquals(ActivityType.STATUS_CHANGE, captor.getValue().getActivityType());
        assertEquals("APPLIED", captor.getValue().getPreviousStatus());
        assertEquals("INTERVIEWING", captor.getValue().getNewStatus());
    }

    @Test
    void referralFalse_clearsReferralAndContact() {
        ApplicationUpdateDTO dto = new ApplicationUpdateDTO();
        dto.setReferral(false);

        Application result = boardService.updateApplication(existing, dto);

        assertFalse(result.isReferral());
        assertNull(result.getReferralContact());
        assertNull(result.getReferralContactId());
    }

    @Test
    void tailoredFalse_clearsTailored() {
        existing.setTailored(true);
        ApplicationUpdateDTO dto = new ApplicationUpdateDTO();
        dto.setTailored(false);

        Application result = boardService.updateApplication(existing, dto);

        assertFalse(result.isTailored());
        assertTrue(result.isReferral());
    }

    @Test
    void referralContactId_linksContact() {
        existing.setReferral(false);
        existing.setReferralContact(null);
        ReferralContact other = new ReferralContact();
        other.setId(8L);
        when(referralContactRepository.findByIdAndUser(8L, user)).thenReturn(Optional.of(other));

        ApplicationUpdateDTO dto = new ApplicationUpdateDTO();
        dto.setReferralContactId(8L);

        Application result = boardService.updateApplication(existing, dto);

        assertTrue(result.isReferral());
        assertSame(other, result.getReferralContact());
        assertEquals(8L, result.getReferralContactId());
    }

    @Test
    void fullBody_appliesAllFields() {
        ApplicationUpdateDTO dto = new ApplicationUpdateDTO();
        dto.setCompanyName("NewCo");
        dto.setRoleName("PM");
        dto.setJobLink("https://newco.com/job");
        dto.setJobDescription("desc");
        dto.setStatus("Offer");
        dto.setTailored(true);
        dto.setReferral(true);
        dto.setApplicationMetadata(Map.of("salary", 100));

        Application result = boardService.updateApplication(existing, dto);

        assertEquals("NewCo", result.getCompanyName());
        assertEquals("PM", result.getRoleName());
        assertEquals("https://newco.com/job", result.getJobLink());
        assertEquals("desc", result.getJobDescription());
        assertEquals("OFFER", result.getStatus());
        assertTrue(result.isTailored());
        assertTrue(result.isReferral());
        assertSame(contact, result.getReferralContact());
        assertEquals(Map.of("salary", 100), result.getApplicationMetadata());
    }
}
