package com.ApplyZap.Tracker.util;

/**
 * Normalizes application status to a single canonical form (uppercase, spaces to
 * underscores) for consistent storage and analytics.
 */
public final class StatusNormalizer {

    private StatusNormalizer() {
    }

    /**
     * Returns the canonical form of the status, or null if null/blank.
     * Trims, uppercases, and replaces spaces with underscores so that
     * "Online Assessment" becomes "ONLINE_ASSESSMENT" and aligns with
     * ApplicationStatus enum names.
     */
    public static String normalize(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return status.trim().toUpperCase().replace(' ', '_');
    }
}
