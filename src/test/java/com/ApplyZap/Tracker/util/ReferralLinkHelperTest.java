package com.ApplyZap.Tracker.util;

import com.ApplyZap.Tracker.model.Application;
import com.ApplyZap.Tracker.model.ReferralContact;
import com.ApplyZap.Tracker.model.User;
import com.ApplyZap.Tracker.repository.ReferralContactRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReferralLinkHelperTest {

    @Mock
    private ReferralContactRepository referralContactRepository;

    private User user;
    private Application application;
    private ReferralContact contact;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(10L);
        application = new Application();
        contact = new ReferralContact();
        contact.setId(5L);
        contact.setUser(user);
        contact.setName("Alex Referrer");
    }

    @Test
    void contactIdSet_linksAndSetsReferralTrue() {
        when(referralContactRepository.findByIdAndUser(5L, user)).thenReturn(Optional.of(contact));

        ReferralLinkHelper.applyReferralLink(application, user, false, 5L, referralContactRepository);

        assertTrue(application.isReferral());
        assertEquals(contact, application.getReferralContact());
    }

    @Test
    void referralTrueWithoutId_setsReferralTrueLeavesContactNull() {
        ReferralLinkHelper.applyReferralLink(application, user, true, null, referralContactRepository);

        assertTrue(application.isReferral());
        assertNull(application.getReferralContact());
    }

    @Test
    void referralFalse_clearsContactAndFlag() {
        application.setReferral(true);
        application.setReferralContact(contact);

        ReferralLinkHelper.applyReferralLink(application, user, false, null, referralContactRepository);

        assertEquals(false, application.isReferral());
        assertNull(application.getReferralContact());
    }

    @Test
    void invalidContactId_throws400StyleError() {
        when(referralContactRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ReferralLinkHelper.applyReferralLink(application, user, true, 99L, referralContactRepository));
        assertTrue(ex.getMessage().contains("not found"));
    }
}
