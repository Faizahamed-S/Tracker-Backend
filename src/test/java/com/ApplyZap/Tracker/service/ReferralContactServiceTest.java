package com.ApplyZap.Tracker.service;

import com.ApplyZap.Tracker.dto.ReferralContactCreateUpdateDTO;
import com.ApplyZap.Tracker.dto.ReferralCustomFieldDefinitionDTO;
import com.ApplyZap.Tracker.model.Application;
import com.ApplyZap.Tracker.model.ReferralContact;
import com.ApplyZap.Tracker.model.User;
import com.ApplyZap.Tracker.repository.ReferralContactRepository;
import com.ApplyZap.Tracker.repository.boardRepository;
import com.ApplyZap.Tracker.repository.userRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReferralContactServiceTest {

    @Mock
    private ReferralContactRepository referralContactRepository;
    @Mock
    private boardRepository boardRepository;
    @Mock
    private userRepository userRepository;
    @Mock
    private userService userService;

    @InjectMocks
    private ReferralContactService referralContactService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setTrackerConfig(new HashMap<>());
        when(userService.getCurrentUser()).thenReturn(user);
    }

    @Test
    void createContact_requiresName() {
        ReferralContactCreateUpdateDTO dto = new ReferralContactCreateUpdateDTO();
        dto.setName("  ");

        assertThrows(IllegalArgumentException.class, () -> referralContactService.createContact(dto));
    }

    @Test
    void createContact_savesOwnedContact() {
        ReferralContactCreateUpdateDTO dto = new ReferralContactCreateUpdateDTO();
        dto.setName("Jordan Lee");
        dto.setCompanyName("Acme");

        when(referralContactRepository.save(any(ReferralContact.class))).thenAnswer(invocation -> {
            ReferralContact saved = invocation.getArgument(0);
            saved.setId(42L);
            return saved;
        });

        var detail = referralContactService.createContact(dto);

        assertEquals(42L, detail.getId());
        assertEquals("Jordan Lee", detail.getName());
        ArgumentCaptor<ReferralContact> captor = ArgumentCaptor.forClass(ReferralContact.class);
        verify(referralContactRepository).save(captor.capture());
        assertEquals(user, captor.getValue().getUser());
    }

    @Test
    void deleteContact_clearsLinkedApplications() {
        ReferralContact contact = new ReferralContact();
        contact.setId(7L);
        contact.setUser(user);
        contact.setName("Pat");

        Application linked = new Application();
        linked.setId(100L);
        linked.setReferral(true);
        linked.setReferralContact(contact);

        when(referralContactRepository.findByIdAndUser(7L, user)).thenReturn(Optional.of(contact));
        when(boardRepository.findByUserAndReferralContact_Id(any(), any(), any())).thenReturn(List.of(linked));

        referralContactService.deleteContact(7L);

        assertFalse(linked.isReferral());
        assertEquals(null, linked.getReferralContact());
        verify(boardRepository).saveAll(List.of(linked));
        verify(referralContactRepository).delete(contact);
    }

    @Test
    void replaceFieldTemplate_mergesIntoTrackerConfig() {
        Map<String, Object> existing = new HashMap<>();
        existing.put("columns", List.of("Applied"));
        user.setTrackerConfig(existing);

        List<ReferralCustomFieldDefinitionDTO> fields = List.of(
                new ReferralCustomFieldDefinitionDTO("met_at", "Met At", 0));

        referralContactService.replaceFieldTemplate(fields);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        Map<String, Object> config = userCaptor.getValue().getTrackerConfig();
        assertTrue(config.containsKey("columns"));
        assertTrue(config.containsKey(ReferralContactService.REFERRAL_CUSTOM_FIELDS_KEY));
    }

    @Test
    void getContact_wrongOwner_throwsSecurityException() {
        when(referralContactRepository.findByIdAndUser(3L, user)).thenReturn(Optional.empty());

        assertThrows(SecurityException.class, () -> referralContactService.getContact(3L));
    }
}
