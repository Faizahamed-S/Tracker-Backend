package com.ApplyZap.Tracker.util;

import com.ApplyZap.Tracker.model.Application;
import com.ApplyZap.Tracker.model.ReferralContact;
import com.ApplyZap.Tracker.model.User;
import com.ApplyZap.Tracker.repository.ReferralContactRepository;

/**
 * Keeps {@link Application#isReferral()} and {@code referral_contact_id} in sync per CRM rules.
 */
public final class ReferralLinkHelper {

    private ReferralLinkHelper() {
    }

    /**
     * @param referral          explicit referral flag from request (create/update)
     * @param referralContactId optional CRM contact id owned by {@code user}
     */
    public static void applyReferralLink(
            Application application,
            User user,
            boolean referral,
            Long referralContactId,
            ReferralContactRepository referralContactRepository) {
        if (referralContactId != null) {
            ReferralContact contact = referralContactRepository.findByIdAndUser(referralContactId, user)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Referral contact not found or not owned by you"));
            application.setReferralContact(contact);
            application.setReferral(true);
            return;
        }
        if (!referral) {
            application.setReferral(false);
            application.setReferralContact(null);
            return;
        }
        application.setReferral(true);
    }
}
