package com.ApplyZap.Tracker.util;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Normalizes job URLs for collaborative group deduplication (Option A).
 * Keeps scheme + host + path; strips query string and fragment; lowercases host.
 */
public final class GroupUrlNormalizer {

    private GroupUrlNormalizer() {
    }

    /**
     * Returns a canonical URL for dedupe: scheme + lowercase host + path; no query or fragment.
     * Returns null if input is null/blank or not a valid URL.
     */
    public static String normalize(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String trimmed = url.trim();
        try {
            URI uri = new URI(trimmed);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            String path = uri.getRawPath();
            if (scheme == null || host == null) {
                return null;
            }
            if (path == null || path.isEmpty()) {
                path = "/";
            }
            return scheme + "://" + host.toLowerCase() + path;
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
