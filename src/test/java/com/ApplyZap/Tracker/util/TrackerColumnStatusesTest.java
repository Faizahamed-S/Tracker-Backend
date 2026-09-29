package com.ApplyZap.Tracker.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrackerColumnStatusesTest {

    @Test
    void normalizesDedupesAndKeepsOrder() {
        Map<String, Object> config = Map.of("columns", List.of(
                Map.of("id", "c1", "title", "Wishlist"),
                Map.of("id", "c2", "title", "Phone Screen"),
                Map.of("id", "c3", "title", "wishlist"),
                Map.of("id", "c4", "title", "Applied")));

        assertEquals(List.of("WISHLIST", "PHONE_SCREEN", "APPLIED"),
                TrackerColumnStatuses.fromTrackerConfig(config));
    }

    @Test
    void handlesNameLabelAndPlainStringColumns() {
        Map<String, Object> config = Map.of("columns", List.of(
                Map.of("name", "On Site"),
                Map.of("label", "Offer"),
                "Ghosted",
                Map.of("title", " ", "name", "Rejected")));

        assertEquals(List.of("ON_SITE", "OFFER", "GHOSTED", "REJECTED"),
                TrackerColumnStatuses.fromTrackerConfig(config));
    }

    @Test
    void emptyForNullOrMissingColumns() {
        assertTrue(TrackerColumnStatuses.fromTrackerConfig(null).isEmpty());
        assertTrue(TrackerColumnStatuses.fromTrackerConfig(new HashMap<>()).isEmpty());
        assertTrue(TrackerColumnStatuses.fromTrackerConfig(Map.of("columns", List.of())).isEmpty());
        assertTrue(TrackerColumnStatuses.fromTrackerConfig(Map.of("columns", "bad")).isEmpty());
    }
}
